package com.deaddiesel.mods.guide;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GuideScreen extends Screen {
    private static final int SIDEBAR_WIDTH = 115;
    private static final int SIDEBAR_X = 5;
    private static final int CONTROL_PANEL_WIDTH = 160;
    private static final int SCROLL_BAR_X_OFFSET = 12;
    private static final int SCROLL_BAR_WIDTH = 6;
    private static final int LINE_HEIGHT = 12;
    private static final int ITEM_ROW_HEIGHT = 22;
    private static final int BG_COLOR = 0xCC1A1A1A;
    private static final int BORDER_COLOR = 0xFF4A4A4A;
    private static final int SCROLL_TRACK_COLOR = 0x55111111;
    private static final int SCROLL_THUMB_COLOR = 0xFF8B8B8B;
    private static final int HOVER_TOOLTIP_Z = 2000;

    private static final int BUTTON_HEIGHT = 16;
    private static final int BUTTON_SPACING = 18;
    private static final int TOP_RESERVED = 28;
    private static final int BOTTOM_BUTTONS_HEIGHT = 88;
    private static final int MAX_BUTTONS_PER_PAGE = 15;

    private int actualMaxPages = 1;
    private boolean isDraggingScroll = false;

    private final String bookNamespace;
    private final GuideNavigator navigator;
    private final GuideRenderer renderer = new GuideRenderer();
    private final GuideStructurePanel structurePanel = new GuideStructurePanel();

    private int lastRenderWidth, lastRenderHeight;
    private GuideEditBox searchBox;
    private final List<Button> rightPanelButtons = new ArrayList<>();
    private Button projectionButton;

    private int searchDebounceTicks = 0;
    private int currentMenuPage = 0;

    public GuideScreen() {
        this(GuideMod.MODID);
    }

    public GuideScreen(String namespace) {
        super(Component.translatable("guide.screen.title"));
        this.bookNamespace = namespace;
        this.navigator = new GuideNavigator(bookNamespace);
    }

    private String buildMdPath(String lang, String fileName) {
        String normalizedLang = lang.toLowerCase().replace("-", "_");
        String pathWithLang = "guide/chapters/" + normalizedLang + "/" + fileName + ".md";
        String pathDefault = "guide/chapters/" + fileName + ".md";

        var resourceManager = Minecraft.getInstance().getResourceManager();
        var locWithLang = ResourceLocation.fromNamespaceAndPath(bookNamespace, pathWithLang);
        if (resourceManager.getResource(locWithLang).isPresent()) {
            return pathWithLang;
        }
        var locDefault = ResourceLocation.fromNamespaceAndPath(bookNamespace, pathDefault);
        if (resourceManager.getResource(locDefault).isPresent()) {
            return pathDefault;
        }
        return "guide/chapters/ru_ru/" + fileName + ".md";
    }

    @SuppressWarnings("SpellCheckingInspection")
    private int getSearchBoxHighlight() {
        if (searchBox == null) return 0;
        try {
            java.lang.reflect.Field field = net.minecraft.client.gui.components.EditBox.class.getDeclaredField("highlightPos");
            field.setAccessible(true);
            return field.getInt(searchBox);
        } catch (Exception e) {
            return searchBox.getCursorPosition();
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private void setSearchBoxHighlight(int highlight) {
        if (searchBox == null) return;
        try {
            java.lang.reflect.Field field = net.minecraft.client.gui.components.EditBox.class.getDeclaredField("highlightPos");
            field.setAccessible(true);
            field.setInt(searchBox, highlight);
        } catch (Exception ignored) {}
    }

    @Override
    protected void init() {
        super.init();

        String oldText = (searchBox != null) ? searchBox.getValue() : "";
        boolean wasFocused = (searchBox != null) && searchBox.isFocused();
        int oldCursor = (searchBox != null) ? searchBox.getCursorPosition() : 0;
        int oldHighlight = getSearchBoxHighlight();

        clearWidgets();
        rightPanelButtons.clear();

        searchBox = new GuideEditBox(font, 10, 10, 104, 14, Component.translatable("guide.search.placeholder"));
        searchBox.setValue(oldText);
        searchBox.setFocused(wasFocused);
        searchBox.setCursorPosition(oldCursor);
        setSearchBoxHighlight(oldHighlight);

        if (!oldText.isEmpty()) {
            searchBox.setSuggestion("");
        }

        searchBox.setResponder(text -> {
            searchDebounceTicks = 4;
            if (!text.isEmpty()) {
                searchBox.setSuggestion("");
            } else {
                searchBox.setSuggestion(Component.translatable("guide.search.placeholder").getString());
            }
            renderer.scrollOffset = 0;
        });
        addRenderableWidget(searchBox);

        if (navigator.activeItemFilterOverride != null) {
            searchBox.setWidth(86);
            Button clearFilterBtn = Button.builder(Component.literal("§c×"), (btn) -> {
                navigator.activeItemFilterOverride = null;
                rebuildWidgets();
            }).bounds(98, 9, 16, 16).build();
            addRenderableWidget(clearFilterBtn);
        }

        navigator.loadChaptersFromIndexFile();
        generateMenuButtons();
        initControlButtons();
        initUtilityButtons();

        loadChapterContentDirectly();
        updateElementsVisibility();
    }

    private void initControlButtons() {
        int ctrlX = width - 160;
        int panelLeft = ctrlX + 10;
        int btnH = 16;
        int gap = 4;
        int bottomSafe = 8;

        int rotBtnY = height - bottomSafe - btnH;
        int layerBtnY = rotBtnY - btnH - gap;

        structurePanel.syncLayout(panelLeft, layerBtnY);

        addRightBtn(Button.builder(Component.translatable("guide.button.zoom_out"),
                        b -> structurePanel.zoomScale = Math.max(0.25f, structurePanel.zoomScale - 0.15f))
                .bounds(panelLeft, 10, 68, btnH).build());
        addRightBtn(Button.builder(Component.translatable("guide.button.zoom_in"),
                        b -> structurePanel.zoomScale = Math.min(1.4f, structurePanel.zoomScale + 0.15f))
                .bounds(panelLeft + 72, 10, 68, btnH).build());

        addRightBtn(Button.builder(Component.translatable("guide.button.layer_down"),
                        b -> { if (structurePanel.currentRenderLayer > -1) structurePanel.currentRenderLayer--; })
                .bounds(panelLeft, layerBtnY, 44, btnH).build());
        addRightBtn(Button.builder(Component.translatable("guide.button.layer_all"),
                        b -> structurePanel.currentRenderLayer = -1)
                .bounds(panelLeft + 48, layerBtnY, 44, btnH).build());
        addRightBtn(Button.builder(Component.translatable("guide.button.layer_up"),
                        b -> { if (structurePanel.currentRenderLayer < 16) structurePanel.currentRenderLayer++; })
                .bounds(panelLeft + 96, layerBtnY, 44, btnH).build());

        addRightBtn(Button.builder(Component.literal("◀"),
                        b -> { structurePanel.isAutoRotating = false; structurePanel.rotationY -= 15.0f; })
                .bounds(panelLeft, rotBtnY, 44, btnH).build());
        addRightBtn(Button.builder(Component.translatable("guide.button.rot_auto"),
                        b -> structurePanel.isAutoRotating = !structurePanel.isAutoRotating)
                .bounds(panelLeft + 48, rotBtnY, 44, btnH).build());
        addRightBtn(Button.builder(Component.literal("▶"),
                        b -> { structurePanel.isAutoRotating = false; structurePanel.rotationY += 15.0f; })
                .bounds(panelLeft + 96, rotBtnY, 44, btnH).build());
    }

    private void addRightBtn(Button b) { rightPanelButtons.add(b); addRenderableWidget(b); }

    private void initUtilityButtons() {
        int bottomRowY = height - 24;
        int projRowY = height - 64;

        projectionButton = Button.builder(Component.translatable("guide.button.projection"), btn -> {
            var tabs = structurePanel.getStructureTabs();
            int activeIndex = structurePanel.getActiveStructureIndex();

            ResourceLocation structureLoc = null;
            String displayName = "";

            if (!tabs.isEmpty() && activeIndex < tabs.size()) {
                var activeTab = tabs.get(activeIndex);
                structureLoc = activeTab.location();

                try {
                    for (java.lang.reflect.Field field : activeTab.getClass().getDeclaredFields()) {
                        if (field.getType() == String.class) {
                            field.setAccessible(true);
                            displayName = (String) field.get(activeTab);
                            break;
                        }
                    }
                } catch (Exception e) {
                    displayName = "";
                }
            } else {
                String chapterFile = getCurrentChapterFile();
                if (!chapterFile.isEmpty()) {
                    structureLoc = ResourceLocation.fromNamespaceAndPath(bookNamespace, "structures/" + chapterFile);
                    displayName = chapterFile;
                }
            }

            if (displayName == null || displayName.isEmpty() && structureLoc != null) {
                displayName = structureLoc.getPath().replace("structures/", "").replace("_", " ");
            }

            if (structureLoc != null) {
                PlacementProjector.setActiveStructure(structureLoc, displayName);
                this.onClose();
            }
        }).bounds(10, projRowY, 104, 16).build();
        addRenderableWidget(projectionButton);

        Component favFilterComp = navigator.isFavoritesFilterActive
                ? Component.translatable("guide.button.fav_filter_on")
                : Component.translatable("guide.button.fav_filter_off");

        Button btnFavoritesToggle = Button.builder(favFilterComp, btn -> {
            navigator.isFavoritesFilterActive = !navigator.isFavoritesFilterActive;
            rebuildWidgets();
        }).bounds(10, bottomRowY, 50, 16).build();
        addRenderableWidget(btnFavoritesToggle);

        String currentFile = (navigator.activeSubmenuFileOverride != null)
                ? navigator.activeSubmenuFileOverride
                : getCurrentChapterFile();

        Component favAddComp = navigator.isFavorite(currentFile)
                ? Component.translatable("guide.button.fav_add_on")
                : Component.translatable("guide.button.fav_add_off");

        Button btnAddToFavorites = Button.builder(favAddComp, btn -> {
            if (!currentFile.isEmpty()) {
                navigator.toggleFavorite(currentFile);
                rebuildWidgets();
            }
        }).bounds(64, bottomRowY, 50, 16).build();
        addRenderableWidget(btnAddToFavorites);
    }

    @Override
    public void tick() {
        super.tick();
        if (searchDebounceTicks > 0) {
            searchDebounceTicks--;
            if (searchDebounceTicks == 0) {
                rebuildWidgets();
            }
        }
        renderer.loadedGifs.forEach(AnimatedGif::tick);
        renderer.loadedMobs.forEach(MobEntityRenderer::tick);
    }

    private void generateMenuButtons() {
        String query = searchBox.getValue().toLowerCase().trim();
        String lang = Minecraft.getInstance().getLanguageManager().getSelected();

        var chapterFiles = navigator.getChapterFiles();
        var chapterNames = navigator.getChapterNames();

        if (navigator.currentChapterIndex >= chapterFiles.size()) {
            navigator.currentChapterIndex = Math.max(0, chapterFiles.size() - 1);
        }

        int availableHeight = height - TOP_RESERVED - BOTTOM_BUTTONS_HEIGHT;
        int maxButtonsPerPage = Math.min(MAX_BUTTONS_PER_PAGE, Math.max(5, availableHeight / BUTTON_SPACING));

        int buttonWidth = Math.min(104, SIDEBAR_WIDTH - 10);

        List<Runnable> totalMenuRegistry = new ArrayList<>();

        for (int i = 0; i < chapterFiles.size(); i++) {
            final int index = i;
            final String file = chapterFiles.get(i);
            final String visibleName = chapterNames.get(i);

            if (navigator.activeItemFilterOverride != null) {
                var boundChapters = GuideNavigator.getItemToChapterMap().get(navigator.activeItemFilterOverride);
                if (boundChapters == null || !boundChapters.contains(file)) {
                    continue;
                }
            }

            if (navigator.isFavoritesFilterActive) {
                boolean chapterOrSubInFav = navigator.isFavorite(file);
                if (!chapterOrSubInFav) {
                    String mdPath = buildMdPath(lang, file);
                    ResourceLocation loc = ResourceLocation.parse(bookNamespace + ":" + mdPath);
                    var resOpt = Minecraft.getInstance().getResourceManager().getResource(loc);
                    if (resOpt.isPresent()) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resOpt.get().open(), StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.trim().startsWith("@submenu:")) {
                                    for (String sub : line.replace("@submenu:", "").trim().split(",")) {
                                        if (navigator.isFavorite(sub.trim())) { chapterOrSubInFav = true; break; }
                                    }
                                    break;
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }
                if (!chapterOrSubInFav) continue;
            }

            String mdPath = buildMdPath(lang, file);
            ResourceLocation loc = ResourceLocation.parse(bookNamespace + ":" + mdPath);
            List<String> submenuItems = new ArrayList<>();

            var resOpt = Minecraft.getInstance().getResourceManager().getResource(loc);
            if (resOpt.isPresent()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resOpt.get().open(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().startsWith("@submenu:")) {
                            for (String sub : line.replace("@submenu:", "").trim().split(",")) {
                                String subFile = sub.trim();
                                if (!subFile.isEmpty()) submenuItems.add(subFile);
                            }
                            break;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            boolean hasSubmenu = !submenuItems.isEmpty();

            List<String> matchingSubs = new ArrayList<>();
            for (String subFile : submenuItems) {
                String subName = subFile;
                int subIdx = chapterFiles.indexOf(subFile);
                if (subIdx != -1 && subIdx < chapterNames.size()) {
                    subName = chapterNames.get(subIdx);
                }
                if (subName.toLowerCase().contains(query)) {
                    matchingSubs.add(subFile);
                }
            }

            boolean chapterMatches = visibleName.toLowerCase().contains(query);
            boolean submenuMatches = !matchingSubs.isEmpty();

            if (!query.isEmpty() && !chapterMatches && !submenuMatches) continue;

            final String label = navigator.isFavorite(file) ? "§e★§r " + visibleName : visibleName;
            boolean isDirectlyActive = (index == navigator.currentChapterIndex && navigator.activeSubmenuFileOverride == null);
            boolean isParentOfOpenSub = java.util.Objects.equals(file, navigator.parentChapterOfFile);
            boolean isActive = isDirectlyActive || isParentOfOpenSub;

            totalMenuRegistry.add(() -> {
                Button menuButton = Button.builder(Component.literal(label), (btn) -> {
                    navigator.currentChapterIndex = index;
                    renderer.scrollOffset = 0;
                    structurePanel.currentRenderLayer = -1;
                    navigator.activeSubmenuFileOverride = null;
                    if (!hasSubmenu) navigator.parentChapterOfFile = null;
                    loadChapterContent();
                }).bounds(10, 0, buttonWidth, BUTTON_HEIGHT).build();

                menuButton.active = !isActive;
                addRenderableWidget(menuButton);
            });

            boolean shouldExpand = isActive || (!query.isEmpty() && submenuMatches);

            if (hasSubmenu && shouldExpand) {
                List<String> subsToShow = query.isEmpty() ? submenuItems : matchingSubs;

                for (String subFile : subsToShow) {
                    final String finalSubFile = subFile;
                    String subName = subFile;
                    int subIdx = chapterFiles.indexOf(subFile);
                    if (subIdx != -1 && subIdx < chapterNames.size()) {
                        subName = chapterNames.get(subIdx);
                    }

                    if (navigator.isFavoritesFilterActive && !navigator.isFavorite(subFile)) continue;
                    if (!query.isEmpty() && !subName.toLowerCase().contains(query)) continue;

                    final String subLabel = navigator.isFavorite(subFile) ? "  §e★§r " + subName : "  • " + subName;
                    final boolean isSubActive = subFile.equals(navigator.activeSubmenuFileOverride);

                    totalMenuRegistry.add(() -> {
                        Button subBtn = Button.builder(Component.literal(subLabel), (btn) -> {
                            renderer.scrollOffset = 0;
                            structurePanel.currentRenderLayer = -1;
                            loadSubmenuChapterContent(finalSubFile);
                        }).bounds(10, 0, buttonWidth, BUTTON_HEIGHT).build();

                        subBtn.active = !isSubActive;
                        addRenderableWidget(subBtn);
                    });
                }
            }
        }

        int totalItems = totalMenuRegistry.size();
        int calculatedPages = totalItems / maxButtonsPerPage;
        if (totalItems % maxButtonsPerPage != 0) {
            calculatedPages++;
        }
        if (calculatedPages == 0) calculatedPages = 1;

        final int maxPages = calculatedPages;

        if (currentMenuPage >= maxPages) currentMenuPage = maxPages - 1;
        if (currentMenuPage < 0) currentMenuPage = 0;

        this.actualMaxPages = maxPages;

        int startIndex = currentMenuPage * maxButtonsPerPage;
        int endIndex = Math.min(startIndex + maxButtonsPerPage, totalItems);

        int buttonY = TOP_RESERVED;
        int widgetsBefore = this.children().size();

        for (int k = startIndex; k < endIndex; k++) {
            totalMenuRegistry.get(k).run();

            if (this.children().size() > widgetsBefore) {
                var newWidget = this.children().get(this.children().size() - 1);
                if (newWidget instanceof net.minecraft.client.gui.components.AbstractWidget widget) {
                    widget.setY(buttonY);
                }
                widgetsBefore = this.children().size();
            }
            buttonY += BUTTON_SPACING;
        }

        if (maxPages > 1) {
            int navY = height - 44;

            Button prevBtn = Button.builder(Component.literal("◀"), (btn) -> {
                if (currentMenuPage > 0) { currentMenuPage--; rebuildWidgets(); }
            }).bounds(10, navY, 20, 14).build();
            prevBtn.active = (currentMenuPage > 0);
            addRenderableWidget(prevBtn);

            Button nextBtn = Button.builder(Component.literal("▶"), (btn) -> {
                if (currentMenuPage < maxPages - 1) { currentMenuPage++; rebuildWidgets(); }
            }).bounds(94, navY, 20, 14).build();
            nextBtn.active = (currentMenuPage < maxPages - 1);
            addRenderableWidget(nextBtn);
        }
    }

    private void loadSubmenuChapterContent(String subFile) {
        try {
            if (subFile == null || subFile.trim().isEmpty()) return;

            navigator.activeSubmenuFileOverride = subFile.trim();
            navigator.parentChapterOfFile = null;
            renderer.scrollOffset = 0;
            structurePanel.currentRenderLayer = -1;

            String lang = Minecraft.getInstance().getLanguageManager().getSelected();
            for (String file : navigator.getChapterFiles()) {
                String mdPath = buildMdPath(lang, file);
                ResourceLocation loc = ResourceLocation.parse(bookNamespace + ":" + mdPath);
                var resOpt = Minecraft.getInstance().getResourceManager().getResource(loc);
                if (resOpt.isPresent()) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resOpt.get().open(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.trim().startsWith("@submenu:")) {
                                for (String sub : line.replace("@submenu:", "").trim().split(",")) {
                                    if (sub.trim().equalsIgnoreCase(navigator.activeSubmenuFileOverride)) {
                                        navigator.parentChapterOfFile = file;
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    } catch (Exception ignored) {}
                }
                if (navigator.parentChapterOfFile != null) break;
            }

            loadChapterContent();
        } catch (Exception e) {
            navigator.activeSubmenuFileOverride = null;
            navigator.parentChapterOfFile = null;
        }
    }

    private void loadChapterContent() {
        loadChapterContentDirectly();
        updateElementsVisibility();
        rebuildWidgets();
    }

    public void addStructureError(String translationKey) {
        renderer.rawTextLines.add("§c" + Component.translatable(translationKey).getString());
    }

    private void loadChapterContentDirectly() {
        renderer.clear();
        structurePanel.clearTabs();
        StructureRenderer.resetCache();

        var chapterFiles = navigator.getChapterFiles();
        if (chapterFiles.isEmpty()) {
            renderer.rawTextLines.add("§c" + Component.translatable("guide.error.chapter_list_empty").getString());
            renderer.originalMarkdownLines.add("");
            return;
        }

        String lang = Minecraft.getInstance().getLanguageManager().getSelected();
        String activeFile = navigator.activeSubmenuFileOverride != null
                ? navigator.activeSubmenuFileOverride
                : chapterFiles.get(navigator.currentChapterIndex);

        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(bookNamespace, buildMdPath(lang, activeFile));
        if (Minecraft.getInstance().getResourceManager().getResource(loc).isEmpty()) {
            renderer.rawTextLines.add("§c" + Component.translatable("guide.error.file_not_found", activeFile).getString());
            renderer.originalMarkdownLines.add("");
            return;
        }

        List<MarkdownParser.ParsedLine> rawLines = MarkdownParser.parseMarkdownFile(loc);
        if (rawLines.isEmpty()) {
            renderer.rawTextLines.add("§c" + Component.translatable("guide.error.file_read").getString());
            renderer.originalMarkdownLines.add("");
            return;
        }

        String pendingTabName = null;
        int structureCount = 0;
        final int MAX_STRUCTURES = 3;

        for (MarkdownParser.ParsedLine p : rawLines) {
            String line = p.text();
            if (line == null) continue;

            if (line.startsWith("@tab:")) {
                pendingTabName = line.substring(5).trim();
            }
            else if (line.startsWith("@bind:")) {
                pendingTabName = null;
            }
            else if (line.startsWith("@structure:")) {
                if (structureCount >= MAX_STRUCTURES) { pendingTabName = null; continue; }
                String rawName = line.substring(11);
                if (rawName.startsWith(" ") || rawName.isEmpty()) { pendingTabName = null; continue; }

                String structName = rawName.trim();
                ResourceLocation structLoc = ResourceLocation.fromNamespaceAndPath(bookNamespace, "structures/" + structName);

                structureCount++;
                boolean hasCustomName = (pendingTabName != null && !pendingTabName.isEmpty());

                structurePanel.addStructureTab(new GuideModels.StructureTabEntry(structLoc, hasCustomName ? pendingTabName : "", hasCustomName));
                pendingTabName = null;
            }
            else if (line.startsWith("@matrix_craft:")) {
                renderer.originalMarkdownLines.add(line.trim());
                renderer.rawTextLines.add("@matrix_craft:");
                renderer.linkTargetsPerLine.add(new ArrayList<>());
            }
            else if (line.startsWith("@inline_item:")) {
                renderer.originalMarkdownLines.add(line.trim());
                renderer.rawTextLines.add("@inline_item:");
                renderer.linkTargetsPerLine.add(new ArrayList<>());
            }
            else if (line.startsWith("@image:")) {
                String imgData = line.substring(7).trim();
                String[] parts = imgData.split(",");
                String path = parts[0];
                int drawW = 150, drawH = 150;
                String align = "left";

                if (parts.length > 1) {
                    try { drawW = Integer.parseInt(parts[1].trim()); } catch (NumberFormatException e) { align = parts[1].trim().toLowerCase(); }
                }
                if (parts.length > 2) {
                    try { drawH = Integer.parseInt(parts[2].trim()); } catch (NumberFormatException e) { align = parts[2].trim().toLowerCase(); }
                }
                if (parts.length > 3) {
                    align = parts[3].trim().toLowerCase();
                }

                renderer.imagesToRender.add(new GuideRenderer.ImageInfo(path, drawW, drawH, align));
                renderer.originalMarkdownLines.add(line.trim());
                renderer.rawTextLines.add("");
                renderer.linkTargetsPerLine.add(new ArrayList<>());
            }
            else if (line.startsWith("@gif:")) {
                String gifData = line.substring(5).trim();
                String[] parts = gifData.split(",");

                String path = parts[0];
                int drawW = 128, drawH = 128;
                String align = "center";

                if (parts.length > 1) {
                    try { drawW = Integer.parseInt(parts[1].trim()); }
                    catch (NumberFormatException e) { align = parts[1].trim().toLowerCase(); }
                }
                if (parts.length > 2) {
                    try { drawH = Integer.parseInt(parts[2].trim()); }
                    catch (NumberFormatException e) { align = parts[2].trim().toLowerCase(); }
                }
                if (parts.length > 3) {
                    align = parts[3].trim().toLowerCase();
                }

                renderer.gifsToRender.add(new GuideRenderer.GifInfo(path, drawW, drawH, align));
                renderer.originalMarkdownLines.add(line.trim());
                renderer.rawTextLines.add("");
                renderer.linkTargetsPerLine.add(new ArrayList<>());
            }
            else if (line.startsWith("@mob:")) {
                String mobData = line.substring(5).trim();
                String[] parts = mobData.split(",");

                String entityId = parts[0];
                int drawW = 128, drawH = 128;
                String align = "center";

                if (parts.length > 1) {
                    try { drawW = Integer.parseInt(parts[1].trim()); }
                    catch (NumberFormatException e) { align = parts[1].trim().toLowerCase(); }
                }
                if (parts.length > 2) {
                    try { drawH = Integer.parseInt(parts[2].trim()); }
                    catch (NumberFormatException e) { align = parts[2].trim().toLowerCase(); }
                }
                if (parts.length > 3) {
                    align = parts[3].trim().toLowerCase();
                }

                renderer.mobsToRender.add(new GuideRenderer.MobInfo(entityId, drawW, drawH, align));
                renderer.originalMarkdownLines.add(line.trim());
                renderer.rawTextLines.add("");
                renderer.linkTargetsPerLine.add(new ArrayList<>());
            }
            else if (line.startsWith("@item:")) {
                renderer.originalMarkdownLines.add(line.trim());
                renderer.rawTextLines.add("@item:");
                renderer.linkTargetsPerLine.add(new ArrayList<>());
            }
            else if (line.startsWith("@spoiler:") || line.equals("@endspoiler")) {
                renderer.originalMarkdownLines.add(line.trim());
                renderer.rawTextLines.add(line.trim());
                renderer.linkTargetsPerLine.add(new ArrayList<>());
            }
            else {
                String lineText = line.trim();
                if (lineText.isEmpty()) {
                    renderer.rawTextLines.add("");
                    renderer.originalMarkdownLines.add("");
                    renderer.linkTargetsPerLine.add(new ArrayList<>());
                } else {
                    renderer.linkTargetsPerLine.add(renderer.extractLinkTargets(lineText));
                    renderer.originalMarkdownLines.add(lineText);
                    renderer.rawTextLines.add(renderer.processLinksForDisplay(lineText));
                }
            }
        }

        for (GuideRenderer.GifInfo gif : renderer.gifsToRender) {
            AnimatedGif animatedGif = new AnimatedGif();
            String gifPath = gif.texturePath();
            ResourceLocation gifLoc = ResourceLocation.tryParse(gifPath);
            if (gifLoc != null) {
                animatedGif.load(gifLoc);
                renderer.loadedGifs.add(animatedGif);
            }
        }

        for (GuideRenderer.MobInfo mob : renderer.mobsToRender) {
            MobEntityRenderer mobRenderer = new MobEntityRenderer();
            mobRenderer.setEntity(mob.entityId());
            renderer.loadedMobs.add(mobRenderer);
        }
        renderer.rebuildTextLines(width, height, !structurePanel.getStructureTabs().isEmpty());
    }

    private void updateElementsVisibility() {
        boolean hasS = !structurePanel.getStructureTabs().isEmpty();
        for (Button b : rightPanelButtons) b.visible = hasS;
        if (projectionButton != null) projectionButton.visible = hasS;
    }

    @Override protected void rebuildWidgets() { clearWidgets(); init(); }
    @Override public boolean isPauseScreen() { return false; }

    private void renderSidebarFrame(GuiGraphics gui) {
        gui.fill(SIDEBAR_X, SIDEBAR_X, SIDEBAR_X + SIDEBAR_WIDTH, height - SIDEBAR_X, BG_COLOR);
        gui.fill(SIDEBAR_X, SIDEBAR_X, SIDEBAR_X + 1, height - SIDEBAR_X, BORDER_COLOR);
        gui.fill(SIDEBAR_X + SIDEBAR_WIDTH - 1, SIDEBAR_X, SIDEBAR_X + SIDEBAR_WIDTH, height - SIDEBAR_X, BORDER_COLOR);
        gui.fill(SIDEBAR_X, SIDEBAR_X, SIDEBAR_X + SIDEBAR_WIDTH, SIDEBAR_X + 1, BORDER_COLOR);
        gui.fill(SIDEBAR_X, height - SIDEBAR_X - 1, SIDEBAR_X + SIDEBAR_WIDTH, height - SIDEBAR_X, BORDER_COLOR);
    }

    private void renderScrollbar(@javax.annotation.Nonnull GuiGraphics gui) {
        boolean hs = !structurePanel.getStructureTabs().isEmpty();
        int sx = hs ? (width - CONTROL_PANEL_WIDTH - SCROLL_BAR_X_OFFSET) : (width - 20);
        int trackRight = sx + SCROLL_BAR_WIDTH;

        if (renderer.maxScroll > 0) {
            int trackTop = 15;
            int trackBottom = height - 25;

            gui.fill(sx, trackTop, trackRight, trackBottom, SCROLL_TRACK_COLOR);

            int totalH = renderer.rawTextLines.size() * LINE_HEIGHT + (renderer.inlineItems.size() * ITEM_ROW_HEIGHT) + 100;
            int trackHeight = trackBottom - trackTop;
            int th = Math.max(15, (int) (((double) trackHeight / totalH) * trackHeight));

            double sp = (double) renderer.scrollOffset / renderer.maxScroll;
            int tt = trackTop + (int) (sp * (trackHeight - th));

            gui.fill(sx, tt, trackRight, tt + th, SCROLL_THUMB_COLOR);
        }
    }

    @Override
    public void render(@javax.annotation.Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (Minecraft.getInstance().getWindow().getWidth() < 1260) {
            renderBackground(guiGraphics);
            Component msg1 = Component.translatable("guide.warning.window_too_small");
            Component msg2 = Component.translatable("guide.warning.resize_hint");
            guiGraphics.drawCenteredString(font, msg1, width / 2, height / 2 - 10, 0xFF5555);
            guiGraphics.drawCenteredString(font, msg2, width / 2, height / 2 + 5, 0xAAAAAA);
            return;
        }

        if (width != lastRenderWidth || height != lastRenderHeight) {
            renderer.rebuildTextLines(width, height, !structurePanel.getStructureTabs().isEmpty());
            lastRenderWidth = width; lastRenderHeight = height;
        }

        renderBackground(guiGraphics);
        renderSidebarFrame(guiGraphics);

        structurePanel.render(guiGraphics, mouseX, mouseY);
        renderer.renderContent(guiGraphics, mouseX, mouseY, width, height, !structurePanel.getStructureTabs().isEmpty());
        renderScrollbar(guiGraphics);

        if (actualMaxPages > 1) {
            int navY = height - 44;
            String pageText = (currentMenuPage + 1) + " | " + actualMaxPages;
            guiGraphics.drawCenteredString(font, pageText, 5 + SIDEBAR_WIDTH / 2, navY + 3, 0x888888);
        }

        if (structurePanel.isAutoRotating) structurePanel.rotationY = (structurePanel.rotationY + 1.0f) % 360.0f;
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (!structurePanel.getStructureTabs().isEmpty()) {
            var hovered = StructureRenderer.getHoveredBlock();
            if (hovered != null) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, HOVER_TOOLTIP_Z);
                RenderSystem.disableDepthTest();
                guiGraphics.renderTooltip(this.font, hovered.getBlock().getName(), mouseX + 12, mouseY - 12);
                RenderSystem.enableDepthTest();
                guiGraphics.pose().popPose();
            }
        }
    }

    @Override public boolean mouseScrolled(double mx, double my, double amount) {
        if (!Minecraft.getInstance().isSameThread()) return false;
        if (renderer.maxScroll > 0) {
            renderer.scrollOffset = (int) Math.max(0, Math.min(renderer.maxScroll, renderer.scrollOffset - amount * 12));
            return true;
        }
        return super.mouseScrolled(mx, my, amount);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (JeiIntegration.isManagerLoaded()) {
            net.minecraft.world.item.ItemStack hoveredStack = renderer.getItemStackAt(mx, my);
            if (hoveredStack != null && !hoveredStack.isEmpty()) {
                if (button == 0) {
                    JeiIntegration.openRecipes(hoveredStack);
                    return true;
                } else if (button == 1) {
                    JeiIntegration.openUses(hoveredStack);
                    return true;
                }
            }
        }

        if (button == 0 && !renderer.activeRenderedLinks.isEmpty()) {
            for (GuideModels.ClickableTextLine link : renderer.activeRenderedLinks) {
                boolean hit = mx >= link.x() - 2 && mx <= link.x() + link.width() + 2 &&
                        my >= link.y() && my < link.y() + LINE_HEIGHT;

                if (hit) {
                    String target = link.targetFile();
                    if (target.startsWith("spoiler:")) {
                        int idx = Integer.parseInt(target.substring(8));
                        renderer.toggleSpoiler(idx);
                        renderer.rebuildTextLines(width, height, !structurePanel.getStructureTabs().isEmpty());
                        return true;
                    } else if (navigator.navigateToFile(target)) {
                        loadChapterContent();
                        return true;
                    }
                }
            }
        }

        if (button == 0 && structurePanel.handleTabClick(mx, my)) return true;
        if (handleScrollbarClick(mx, my, button)) return true;

        return super.mouseClicked(mx, my, button);
    }

    @Override public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (button == 0 && isDraggingScroll) {
            handleScrollbarDrag(my);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) {
            isDraggingScroll = false;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBox != null && searchBox.isFocused() && keyCode != 256) {
            navigator.activeItemFilterOverride = null;
        }

        if (searchBox != null && searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean charTyped(char c, int m) {
        if (searchBox != null && searchBox.isFocused()) {
            if (searchBox.charTyped(c, m)) return true;
        }
        return super.charTyped(c, m);
    }

    private String getCurrentChapterFile() {
        var files = navigator.getChapterFiles();
        if (navigator.currentChapterIndex >= 0 && navigator.currentChapterIndex < files.size()) {
            return files.get(navigator.currentChapterIndex);
        }
        return "";
    }

    private boolean handleScrollbarClick(double mx, double my, int button) {
        if (button != 0) return false;
        if (renderer.maxScroll <= 0) return false;

        boolean hs = !structurePanel.getStructureTabs().isEmpty();
        int sx = hs ? (width - CONTROL_PANEL_WIDTH - SCROLL_BAR_X_OFFSET) : (width - 20);

        int trackTop = 15;
        int trackBottom = height - 25;

        if (mx >= sx && mx <= sx + SCROLL_BAR_WIDTH) {
            int totalH = renderer.rawTextLines.size() * LINE_HEIGHT + (renderer.inlineItems.size() * ITEM_ROW_HEIGHT) + 100;
            int trackHeight = trackBottom - trackTop;
            int th = Math.max(15, (int) (((double) trackHeight / totalH) * trackHeight));
            double sp = (double) renderer.scrollOffset / renderer.maxScroll;
            int tt = trackTop + (int) (sp * (trackHeight - th));

            if (my >= tt && my <= tt + th) {
                isDraggingScroll = true;
                return true;
            }

            if (my >= trackTop && my <= trackBottom) {
                int pageScroll = (int) ((double) trackHeight * 0.8);
                if (my < tt) renderer.scrollOffset = Math.max(0, renderer.scrollOffset - pageScroll);
                else renderer.scrollOffset = Math.min(renderer.maxScroll, renderer.scrollOffset + pageScroll);
                return true;
            }
        }
        return false;
    }

    private void handleScrollbarDrag(double my) {
        int trackTop = 15;
        int trackBottom = height - 25;
        int trackHeight = trackBottom - trackTop;

        int totalH = renderer.rawTextLines.size() * LINE_HEIGHT + (renderer.inlineItems.size() * ITEM_ROW_HEIGHT) + 100;
        int th = Math.max(15, (int) (((double) trackHeight / totalH) * trackHeight));

        double mouseRatio = (my - trackTop) / (trackHeight - th);
        renderer.scrollOffset = (int) (mouseRatio * renderer.maxScroll);
        renderer.scrollOffset = Math.max(0, Math.min(renderer.maxScroll, renderer.scrollOffset));
    }

    public GuideNavigator getNavigator() {
        return this.navigator;
    }

    @Override
    public void onClose() {
        renderer.clear();
        super.onClose();
    }
}