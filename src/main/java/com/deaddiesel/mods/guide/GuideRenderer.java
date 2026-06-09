package com.deaddiesel.mods.guide;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GuideRenderer {
    private static final int TEXT_START_X = 125;
    private static final int TEXT_START_Y = 20;
    private static final int LINE_HEIGHT = 12;
    private static final int ITEM_ROW_HEIGHT = 22;
    private static final int SCROLL_AREA_TOP = 10;
    private static final int SCROLL_AREA_BOTTOM = 90;
    private static final int CONTROL_PANEL_OFFSET = 160;
    private static final int MIN_TEXT_WIDTH = 100;
    private static final int TEXT_PADDING = 20;
    private static final int INLINE_ITEM_PADDING = 10;

    private static int getScrollbarX(int width, boolean hasStructure) {
        return hasStructure
                ? (width - CONTROL_PANEL_OFFSET - 12)
                : (width - 20);
    }

    private final Map<Integer, DynamicTexture> gifTextureCache = new HashMap<>();
    private final Map<Integer, Boolean> spoilerStates = new HashMap<>();

    public final List<String> rawTextLines = new ArrayList<>();
    public final List<String> originalMarkdownLines = new ArrayList<>();
    public final List<List<String>> linkTargetsPerLine = new ArrayList<>();
    public final List<GuideModels.ClickableTextLine> activeRenderedLinks = new ArrayList<>();
    public final List<GuideModels.ClickableItemZone> activeRenderedItems = new ArrayList<>();
    public final List<String> inlineItems = new ArrayList<>();
    public final List<Item> matrixCraftItems = new ArrayList<>();

    public record ImageInfo(String texturePath, int drawW, int drawH, String align) {}
    public final List<ImageInfo> imagesToRender = new ArrayList<>();

    public record GifInfo(String texturePath, int drawW, int drawH, String align) {}
    public final List<GifInfo> gifsToRender = new ArrayList<>();
    public final List<AnimatedGif> loadedGifs = new ArrayList<>();

    public record MobInfo(String entityId, int drawW, int drawH, String align) {}
    public final List<MobInfo> mobsToRender = new ArrayList<>();
    public final List<MobEntityRenderer> loadedMobs = new ArrayList<>();

    public int scrollOffset = 0;
    public int maxScroll = 0;

    public void clear() {
        rawTextLines.clear();
        originalMarkdownLines.clear();
        linkTargetsPerLine.clear();
        activeRenderedLinks.clear();
        activeRenderedItems.clear();
        inlineItems.clear();
        matrixCraftItems.clear();
        imagesToRender.clear();
        gifsToRender.clear();
        loadedGifs.forEach(AnimatedGif::clear);
        loadedGifs.clear();
        gifTextureCache.values().forEach(DynamicTexture::close);
        gifTextureCache.clear();
        mobsToRender.clear();
        loadedMobs.forEach(MobEntityRenderer::clear);
        loadedMobs.clear();
        spoilerStates.clear();
        scrollOffset = 0;
        maxScroll = 0;
    }

    @SuppressWarnings("unused")
    public List<String> extractLinkTargets(String input) {
        List<String> targets = new ArrayList<>();
        int i = 0;
        boolean inCode = false;

        while (i < input.length()) {
            char c = input.charAt(i);
            if (c == '`') {
                inCode = !inCode;
                i++;
                continue;
            }
            if (inCode) { i++; continue; }

            if (c == '[') {
                int start = i;
                int end = input.indexOf(']', start);
                if (end == -1) { i++; continue; }

                if (end + 1 < input.length() && input.charAt(end + 1) == '(') {
                    int paren = end + 1;
                    int close = input.indexOf(')', paren);
                    if (close != -1) {
                        targets.add(input.substring(paren + 1, close));
                        i = close + 1;
                    } else { i++; }
                } else { i++; }
            } else {
                i++;
            }
        }
        return targets;
    }

    @SuppressWarnings({"unused", "RegExpRedundantEscape"})
    public String processLinksForDisplay(String input) {
        if (input == null || input.isEmpty()) return input;

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?<!`)\\[([^\\]]+)\\]\\(([^\\)]+)\\)(?!`)");
        java.util.regex.Matcher matcher = pattern.matcher(input);

        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String linkText = matcher.group(1);
            matcher.appendReplacement(sb, "§b§n" + linkText + "§r");
        }
        matcher.appendTail(sb);

        return sb.toString().replace("`", "");
    }

    private Component parseFormattedText(String input) {
        MutableComponent result = Component.literal("");
        Style currentStyle = Style.EMPTY;
        int i = 0, len = input.length();
        StringBuilder currentText = new StringBuilder();
        while (i < len) {
            char c = input.charAt(i);
            if (c == '§' && i + 1 < len) {
                char code = input.charAt(i + 1);
                if (!currentText.isEmpty()) {
                    result.append(Component.literal(currentText.toString()).withStyle(currentStyle));
                    currentText.setLength(0);
                }
                if (code == 'r') currentStyle = Style.EMPTY;
                else if (code == 'l') currentStyle = currentStyle.withBold(true);
                else if (code == 'o') currentStyle = currentStyle.withItalic(true);
                else if (code == 'n') currentStyle = currentStyle.withUnderlined(true);
                else if (code == 'm') currentStyle = currentStyle.withStrikethrough(true);
                else if ("0123456789abcdef".indexOf(code) != -1) {
                    net.minecraft.ChatFormatting formatting = net.minecraft.ChatFormatting.getByCode(code);
                    if (formatting != null) {
                        currentStyle = currentStyle.withColor(net.minecraft.network.chat.TextColor.fromLegacyFormat(formatting));
                    }
                }
                i += 2;
            } else { currentText.append(c); i++; }
        }
        if (!currentText.isEmpty()) result.append(Component.literal(currentText.toString()).withStyle(currentStyle));
        return result;
    }

    public void rebuildTextLines(int width, int height, boolean hasStructure) {
        int textX = TEXT_START_X;
        int controlX = width - CONTROL_PANEL_OFFSET;
        int maxTextWidth = hasStructure ? Math.max(MIN_TEXT_WIDTH, controlX - textX - TEXT_PADDING) : Math.max(MIN_TEXT_WIDTH, width - textX - TEXT_PADDING);
        int totalLines = 0;
        var font = Minecraft.getInstance().font;

        for (String line : rawTextLines) {
            totalLines += line.isEmpty() ? 1 : font.split(parseFormattedText(line), maxTextWidth).size();
        }

        int contentHeight = calculateContentHeight(totalLines);

        int visibleHeight = height - SCROLL_AREA_TOP - SCROLL_AREA_BOTTOM + 10;
        maxScroll = Math.max(0, contentHeight - visibleHeight);

        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
    }

    private record LinkRange(String text, String target, int startChar, int endChar) {}

    private List<LinkRange> extractLinkRanges(String input) {
        List<LinkRange> ranges = new ArrayList<>();
        int i = 0;
        boolean inCode = false;

        while (i < input.length()) {
            char c = input.charAt(i);
            if (c == '`') {
                inCode = !inCode;
                i++;
                continue;
            }
            if (inCode) { i++; continue; }

            if (c == '[') {
                int start = i;
                int end = input.indexOf(']', start);
                if (end == -1) { i++; continue; }

                if (end + 1 < input.length() && input.charAt(end + 1) == '(') {
                    int paren = end + 1;
                    int close = input.indexOf(')', paren);

                    if (close != -1) {
                        String linkText = input.substring(start + 1, end);
                        String target = input.substring(paren + 1, close);
                        ranges.add(new LinkRange(linkText, target, start + 1, end));
                        i = close + 1;
                    } else { i++; }
                } else { i++; }
            } else {
                i++;
            }
        }
        return ranges;
    }

    private String extractText(FormattedCharSequence seq) {
        StringBuilder sb = new StringBuilder();
        seq.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });
        return sb.toString();
    }

    private void renderItem3D(net.minecraft.client.gui.GuiGraphics guiGraphics, int x, int y, int size, net.minecraft.world.item.ItemStack stack) {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null || stack.isEmpty()) return;

        var itemRenderer = mc.getItemRenderer();
        var bakedModel = itemRenderer.getModel(stack, null, null, 0);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + size / 2f, y + size / 2f, 150f);

        float scale = (float) size;
        guiGraphics.pose().scale(scale, -scale, scale);

        float renderTicks = mc.getFrameTimeNs() / 1E9f + mc.level.getGameTime() / 20.0f;
        float rot = (renderTicks * 45.0f) % 360f;
        guiGraphics.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees(rot));

        com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
        com.mojang.blaze3d.platform.Lighting.setupForFlatItems();
        com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorTexLightmapShader);

        itemRenderer.render(
                stack,
                net.minecraft.world.item.ItemDisplayContext.NONE,
                false,
                guiGraphics.pose(),
                guiGraphics.bufferSource(),
                15728880,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                bakedModel
        );

        guiGraphics.flush();
        com.mojang.blaze3d.platform.Lighting.setupFor3DItems();
        guiGraphics.pose().popPose();
    }

    private void renderItemTooltip(GuiGraphics guiGraphics, int x, int y, int width, int height,
                                   ItemStack stack, Item item, int mouseX, int mouseY) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal(String.format(
                    Component.translatable("guide.tooltip.entity_name").getString(),
                    stack.getHoverName().getString()
            )));
            ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(item);
            String idStr = (itemKey != null) ? itemKey.toString() : "unknown";
            tooltip.add(Component.literal("§7ID: §e" + idStr));
            tooltip.add(Component.literal(String.format(
                    Component.translatable("guide.tooltip.stack").getString(),
                    stack.getCount(),
                    stack.getMaxStackSize()
            )));

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 500);
            guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), mouseX, mouseY);
            guiGraphics.pose().popPose();
        }
    }

    public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, int width, int height, boolean hasStructure) {
        activeRenderedLinks.clear();
        int textX = TEXT_START_X;
        int currentY = TEXT_START_Y - scrollOffset;
        int controlXForText = width - CONTROL_PANEL_OFFSET;
        int maxTextWidth = hasStructure ? Math.max(MIN_TEXT_WIDTH, controlXForText - textX - TEXT_PADDING) : Math.max(MIN_TEXT_WIDTH, width - textX - TEXT_PADDING);
        var font = Minecraft.getInstance().font;

        int rightBoundary = hasStructure
                ? (width - CONTROL_PANEL_OFFSET - 12)
                : (width - 20);

        int imageIndex = 0;
        int spoilerIndex = 0;
        boolean insideSpoiler = false;
        boolean spoilerOpen = true;

        for (int i = 0; i < originalMarkdownLines.size(); i++) {
            String sourceLine = originalMarkdownLines.get(i);
            if (sourceLine == null) continue;

            if (sourceLine.startsWith("@spoiler:")) {
                String title = sourceLine.substring(9).trim();
                spoilerOpen = spoilerStates.getOrDefault(spoilerIndex, false);
                insideSpoiler = true;

                if (currentY > SCROLL_AREA_TOP && currentY < height - SCROLL_AREA_BOTTOM) {
                    String btn = (spoilerOpen ? "▼ " : "▶ ") + title;
                    int titleWidth = font.width(btn);
                    int maxTitleWidth = rightBoundary - textX - 25;

                    if (titleWidth > maxTitleWidth) {
                        btn = font.plainSubstrByWidth(btn, maxTitleWidth - 10) + "...";
                    }

                    guiGraphics.drawString(font, btn, textX, currentY, 0xFFAA00, false);
                    activeRenderedLinks.add(new GuideModels.ClickableTextLine(textX, currentY, font.width(btn), true, "spoiler:" + spoilerIndex));
                }
                currentY += LINE_HEIGHT + 8;
                spoilerIndex++;
                continue;
            }

            if (sourceLine.equals("@endspoiler")) {
                insideSpoiler = false;
                continue;
            }

            if (insideSpoiler && !spoilerOpen) {
                continue;
            }

            if (sourceLine.startsWith("@inline_item:")) {
                String itemName = sourceLine.substring(13).trim();
                currentY = renderSingleInlineItem(guiGraphics, textX, currentY, mouseX, mouseY, rightBoundary, itemName);
                continue;
            }

            if (sourceLine.startsWith("@matrix_craft:")) {
                String craftData = sourceLine.substring(14).trim();
                String[] tokens = craftData.split(",");

                String align = "left";
                if (tokens.length > 0) {
                    String lastToken = tokens[tokens.length - 1].trim().toLowerCase();
                    if (lastToken.equals("left") || lastToken.equals("center") || lastToken.equals("right")) {
                        align = lastToken;
                        craftData = craftData.substring(0, craftData.lastIndexOf(','));
                    }
                }

                currentY = renderSingleCraftGrid(guiGraphics, textX, currentY, mouseX, mouseY, craftData, align, maxTextWidth);
                continue;
            }

            if (sourceLine.startsWith("@image:")) {
                if (imageIndex < imagesToRender.size()) {
                    ImageInfo img = imagesToRender.get(imageIndex);
                    imageIndex++;

                    int imageX = switch (img.align()) {
                        case "center" -> textX + (maxTextWidth - img.drawW()) / 2;
                        case "right"  -> rightBoundary - img.drawW() - 5;
                        default       -> textX;
                    };

                    boolean visible = isElementVisible(currentY, img.drawH(), height);
                    if (visible && currentY + img.drawH() > SCROLL_AREA_TOP && currentY < height - SCROLL_AREA_BOTTOM) {
                        ResourceLocation tex = ResourceLocation.tryParse(img.texturePath());
                        if (tex != null) {
                            guiGraphics.blit(tex, imageX, currentY, 0, 0, img.drawW(), img.drawH(), img.drawW(), img.drawH());
                        }
                    }

                    currentY += img.drawH() + 5;
                }
                continue;
            }

            if (sourceLine.startsWith("@gif:")) {
                int gifIndex = (int) originalMarkdownLines.subList(0, i).stream()
                        .filter(s -> s.startsWith("@gif:")).count();

                if (gifIndex < gifsToRender.size() && gifIndex < loadedGifs.size()) {
                    GifInfo gif = gifsToRender.get(gifIndex);
                    AnimatedGif animatedGif = loadedGifs.get(gifIndex);
                    boolean visible = isElementVisible(currentY, gif.drawH(), height);

                    if (visible && animatedGif.isLoaded()) {
                        var frame = animatedGif.getCurrentFrame();
                        if (frame != null && currentY + gif.drawH() > SCROLL_AREA_TOP && currentY < height - SCROLL_AREA_BOTTOM) {
                            int imageX = switch (gif.align()) {
                                case "center" -> textX + (maxTextWidth - gif.drawW()) / 2;
                                case "right"  -> {
                                    int scrollbarX = getScrollbarX(width, hasStructure);
                                    yield Math.min(rightBoundary - gif.drawW() - 10, scrollbarX - gif.drawW() - 10);
                                }
                                default -> textX;
                            };

                            DynamicTexture cachedTexture = gifTextureCache.get(gifIndex);

                            if (cachedTexture == null) {
                                var nativeImage = new com.mojang.blaze3d.platform.NativeImage(
                                        frame.getWidth(), frame.getHeight(), true
                                );

                                for (int y = 0; y < frame.getHeight(); y++) {
                                    for (int x = 0; x < frame.getWidth(); x++) {
                                        int rgb = frame.getRGB(x, y);
                                        int r = (rgb >> 16) & 0xFF;
                                        int g = (rgb >> 8) & 0xFF;
                                        int b = rgb & 0xFF;
                                        int a = (rgb >> 24) & 0xFF;
                                        nativeImage.setPixelRGBA(x, y, (r) | (g << 8) | (b << 16) | (a << 24));
                                    }
                                }

                                cachedTexture = new DynamicTexture(nativeImage);
                                gifTextureCache.put(gifIndex, cachedTexture);
                            } else {
                                var nativeImage = cachedTexture.getPixels();
                                if (nativeImage != null) {
                                    for (int y = 0; y < frame.getHeight() && y < nativeImage.getHeight(); y++) {
                                        for (int x = 0; x < frame.getWidth() && x < nativeImage.getWidth(); x++) {
                                            int rgb = frame.getRGB(x, y);
                                            int r = (rgb >> 16) & 0xFF;
                                            int g = (rgb >> 8) & 0xFF;
                                            int b = rgb & 0xFF;
                                            int a = (rgb >> 24) & 0xFF;
                                            nativeImage.setPixelRGBA(x, y, (r) | (g << 8) | (b << 16) | (a << 24));
                                        }
                                    }
                                    cachedTexture.upload();
                                }
                            }

                            ResourceLocation gifTextureLoc = ResourceLocation.fromNamespaceAndPath(
                                    GuideMod.MODID, "gif_frame_" + gifIndex
                            );
                            Minecraft.getInstance().getTextureManager().register(gifTextureLoc, cachedTexture);
                            guiGraphics.blit(gifTextureLoc, imageX, currentY, 0, 0,
                                    gif.drawW(), gif.drawH(), gif.drawW(), gif.drawH());
                        }
                    }
                }

                GifInfo gif = gifsToRender.get(gifIndex);
                currentY += gif.drawH() + 5;
                continue;
            }

            if (sourceLine.startsWith("@mob:")) {
                int mobIndex = (int) originalMarkdownLines.subList(0, i).stream()
                        .filter(s -> s.startsWith("@mob:")).count();

                if (mobIndex < mobsToRender.size() && mobIndex < loadedMobs.size()) {
                    MobInfo mob = mobsToRender.get(mobIndex);
                    MobEntityRenderer mobRenderer = loadedMobs.get(mobIndex);

                    int availableWidth = getAvailableWidth(hasStructure, width);
                    int availableHeight = height - SCROLL_AREA_TOP - SCROLL_AREA_BOTTOM - 40;
                    int finalSize = calculateMobSize(mob, availableWidth, availableHeight);

                    int mobX = switch (mob.align()) {
                        case "center" -> TEXT_START_X + (availableWidth - finalSize) / 2;
                        case "right"  -> rightBoundary - finalSize - 10;
                        default       -> TEXT_START_X;
                    };

                    boolean visible = isElementVisible(currentY, finalSize, height);

                    if (visible && mobRenderer.getEntity() == null) {
                        mobRenderer.setEntity(mob.entityId());
                    }

                    currentY += 10;

                    if (visible && mobRenderer.getEntity() != null && currentY + finalSize > SCROLL_AREA_TOP && currentY < height - SCROLL_AREA_BOTTOM) {
                        mobRenderer.render(guiGraphics.pose(),
                                Minecraft.getInstance().renderBuffers().bufferSource(),
                                mobX, currentY, finalSize, finalSize);

                        mobRenderer.renderTooltip(guiGraphics, mobX, currentY, finalSize, finalSize);
                    }

                    currentY += finalSize + 10;
                }
                continue;
            }

            if (sourceLine.startsWith("@item:")) {
                String[] parts = sourceLine.substring(6).trim().split(",");
                if (parts.length >= 2) {
                    String itemId = parts[0].trim();
                    int w = Integer.parseInt(parts[1].trim());
                    String align = parts.length > 2 ? parts[2].trim() : "center";

                    int leftEdge = TEXT_START_X;
                    int availableWidth = rightBoundary - leftEdge;
                    int finalSize = Math.min(w, availableWidth);
                    finalSize = Math.max(finalSize, 70);

                    int itemX = switch (align) {
                        case "center" -> leftEdge + (availableWidth - finalSize) / 2;
                        case "right"  -> rightBoundary - finalSize - 5;
                        default       -> leftEdge;
                    };

                    Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemId));
                    if (item != null) {
                        ItemStack stack = new ItemStack(item);
                        int itemY = currentY + 10;

                        boolean visible = isElementVisible(currentY, finalSize, height);
                        if (visible && currentY + finalSize > SCROLL_AREA_TOP && currentY < height - SCROLL_AREA_BOTTOM) {
                            renderItem3D(guiGraphics, itemX, itemY, finalSize, stack);
                            renderItemTooltip(guiGraphics, itemX, itemY, finalSize, finalSize, stack, item, mouseX, mouseY);
                        }
                        currentY += finalSize + 10;
                    }
                    continue;
                }
            }

            if (sourceLine.startsWith("@tab:") || sourceLine.startsWith("@structure:") || sourceLine.startsWith("@submenu:")) {
                continue;
            }

            if (i >= rawTextLines.size()) continue;
            String displayLine = rawTextLines.get(i);

            if (displayLine.isEmpty()) {
                currentY += LINE_HEIGHT;
                continue;
            }

            List<LinkRange> linkRanges = extractLinkRanges(sourceLine);
            int linkIdx = 0;

            Component styled = parseFormattedText(displayLine);
            List<FormattedCharSequence> splitLines = font.split(styled, maxTextWidth);

            for (FormattedCharSequence sl : splitLines) {
                if (currentY > SCROLL_AREA_TOP && currentY < height - SCROLL_AREA_BOTTOM) {
                    guiGraphics.drawString(font, sl, textX, currentY, 0xFFFFFF, false);

                    String visualText = extractText(sl);

                    while (linkIdx < linkRanges.size()) {
                        LinkRange lr = linkRanges.get(linkIdx);
                        String linkText = lr.text();

                        int pos = visualText.indexOf(linkText);
                        if (pos != -1) {
                            String before = visualText.substring(0, pos);
                            int xOffset = textX + font.width(Component.literal(before));
                            int linkWidth = font.width(Component.literal(linkText));

                            activeRenderedLinks.add(new GuideModels.ClickableTextLine(xOffset, currentY, linkWidth, true, lr.target()));
                            linkIdx++;
                        } else {
                            break;
                        }
                    }
                }
                currentY += LINE_HEIGHT;
            }
        }
    }

    private int renderSingleInlineItem(GuiGraphics g, int x, int y, int mx, int my, int rightBound, String itemName) {
        int iy = y + 4;

        var screen = Minecraft.getInstance().screen;
        int screenHeight = (screen != null) ? screen.height : Minecraft.getInstance().getWindow().getGuiScaledHeight();

        int mw = rightBound - INLINE_ITEM_PADDING - 5;
        int boxH = ITEM_ROW_HEIGHT - 2;

        if (y > SCROLL_AREA_TOP && y < screenHeight - SCROLL_AREA_BOTTOM) {
            g.fill(x, iy, mw, iy + boxH, 0x550A0A0A);
            renderFrame(g, x, iy, mw, iy + boxH);

            Item it = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemName));
            if (it != null) {
                ItemStack s = new ItemStack(it);
                g.renderFakeItem(s, x + 6, iy + 2);
                activeRenderedItems.add(new GuideModels.ClickableItemZone(x + 6, iy + 2, 16, 16, s));
                g.drawString(Minecraft.getInstance().font, "§6• §7" + s.getHoverName().getString(), x + 26, iy + 6, 0xAAAAAA, false);
                if (mx >= x + 6 && mx <= x + 22 && my >= iy + 2 && my <= iy + 18) {
                    g.renderTooltip(Minecraft.getInstance().font, s, mx, my);
                }
            }
        }
        return y + ITEM_ROW_HEIGHT;
    }

    private int renderSingleCraftGrid(GuiGraphics g, int x, int y, int mx, int my, String craftData, String align, int maxWidth) {
        int cy = y + 6;
        int gridH = 56;
        int gridWidth = 120;

        int craftX = switch (align) {
            case "center" -> x + (maxWidth - gridWidth) / 2;
            case "right"  -> x + maxWidth - gridWidth;
            default       -> x;
        };

        var screen = Minecraft.getInstance().screen;
        int screenHeight = (screen != null) ? screen.height : Minecraft.getInstance().getWindow().getGuiScaledHeight();

        if (y > SCROLL_AREA_TOP && y < screenHeight - SCROLL_AREA_BOTTOM) {
            g.blit(ResourceLocation.parse("minecraft:textures/gui/container/crafting_table.png"), craftX, cy, 29, 16, 120, gridH);
            String[] tokens = craftData.split(",");
            if (tokens.length >= 10) {
                for (int r = 0; r < 3; r++) {
                    for (int c = 0; c < 3; c++) {
                        String t = tokens[1 + r * 3 + c].trim();
                        Item it = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(t));
                        if (it != null && it != Blocks.AIR.asItem()) {
                            int sx = craftX + 1 + c * 18;
                            int sy = cy + 1 + r * 18;
                            ItemStack s = new ItemStack(it);
                            g.renderFakeItem(s, sx, sy);
                            activeRenderedItems.add(new GuideModels.ClickableItemZone(sx, sy, 16, 16, s));
                            if (mx >= sx && mx <= sx + 16 && my >= sy && my <= sy + 16) {
                                g.renderTooltip(Minecraft.getInstance().font, s, mx, my);
                            }
                        }
                    }
                }

                String resToken = tokens[0].trim();
                Item res = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(resToken));
                if (res != null && res != Blocks.AIR.asItem()) {
                    int resX = craftX + 95;
                    int resY = cy + 20;
                    ItemStack resStack = new ItemStack(res);

                    g.renderFakeItem(resStack, resX, resY);
                    activeRenderedItems.add(new GuideModels.ClickableItemZone(resX, resY, 16, 16, resStack));

                    if (mx >= resX && mx <= resX + 16 && my >= resY && my <= resY + 16) {
                        g.renderTooltip(Minecraft.getInstance().font, resStack, mx, my);
                    }
                }
            }
        }
        return y + gridH + 12;
    }

    private void renderFrame(GuiGraphics gui, int x1, int y1, int x2, int y2) {
        int inlineColor = 0x25FFFFFF;
        gui.fill(x1, y1, x2, y1 + 1, inlineColor);
        gui.fill(x1, y2 - 1, x2, y2, inlineColor);
        gui.fill(x1, y1 + 1, x1 + 1, y2 - 1, inlineColor);
        gui.fill(x2 - 1, y1 + 1, x2, y2 - 1, inlineColor);
    }

    private int calculateContentHeight(int totalLines) {
        int contentHeight = totalLines * LINE_HEIGHT;

        for (GifInfo gif : gifsToRender) contentHeight += gif.drawH() + 5;
        for (ImageInfo img : imagesToRender) contentHeight += img.drawH() + 5;
        for (MobInfo mob : mobsToRender) contentHeight += mob.drawH() + 20;

        int spoilerIdx = 0;
        boolean insideSpoiler = false;

        for (String sourceLine : originalMarkdownLines) {
            if (sourceLine.startsWith("@spoiler:")) {
                insideSpoiler = true;
                contentHeight += LINE_HEIGHT + 8;
                spoilerIdx++;
                continue;
            }
            if (sourceLine.equals("@endspoiler")) {
                insideSpoiler = false;
                continue;
            }
            if (insideSpoiler && !spoilerStates.getOrDefault(spoilerIdx - 1, false)) {
                continue;
            }

            if (sourceLine.startsWith("@inline_item:")) contentHeight += ITEM_ROW_HEIGHT;
            else if (sourceLine.startsWith("@matrix_craft:")) contentHeight += 56 + 12;
            else if (sourceLine.startsWith("@item:")) {
                String[] parts = sourceLine.substring(6).trim().split(",");
                if (parts.length >= 2) {
                    try {
                        int size = Integer.parseInt(parts[1].trim());
                        contentHeight += Math.max(size, 50) + 20;
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return contentHeight + 25;
    }

    public ItemStack getItemStackAt(double mx, double my) {
        if (!activeRenderedItems.isEmpty()) {
            for (GuideModels.ClickableItemZone zone : activeRenderedItems) {
                if (mx >= zone.x() && mx <= zone.x() + zone.width() &&
                        my >= zone.y() && my <= zone.y() + zone.height()) {
                    return zone.stack();
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private int calculateMobSize(MobInfo mob, int availableWidth, int availableHeight) {
        int maxMobWidth = switch (mob.align()) {
            case "center" -> Math.min(mob.drawW(), availableWidth);
            case "right"  -> Math.min(mob.drawW(), availableWidth / 2);
            default       -> Math.min(mob.drawW(), availableWidth * 2 / 3);
        };
        int maxMobHeight = Math.min(mob.drawH(), availableHeight);
        int finalSize = Math.min(maxMobWidth, maxMobHeight);
        return Math.max(finalSize, 30);
    }

    private boolean isElementVisible(int y, int h, int screenH) {
        int margin = 200;
        return y + h > -margin && y < screenH + margin;
    }

    public void toggleSpoiler(int index) {
        spoilerStates.put(index, !spoilerStates.getOrDefault(index, false));
    }

    private int getAvailableWidth(boolean hasStructure, int width) {
        return hasStructure
                ? (width - CONTROL_PANEL_OFFSET - TEXT_START_X - TEXT_PADDING)
                : (width - TEXT_START_X - TEXT_PADDING - 20);
    }
}