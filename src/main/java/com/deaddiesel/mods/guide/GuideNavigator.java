package com.deaddiesel.mods.guide;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GuideNavigator {
    private final String bookNamespace;

    private final List<ChapterInfo> chapters = new ArrayList<>();
    private final Set<String> chapterFileSet = new HashSet<>();
    private final Map<String, String> submenuParentMap = new HashMap<>();
    private final Map<String, String> titleCache = new HashMap<>();
    private static final Map<String, List<String>> itemToChapterMap = new HashMap<>();

    public int currentChapterIndex = 0;
    public String activeSubmenuFileOverride = null;
    public String parentChapterOfFile = null;
    public boolean isFavoritesFilterActive = false;
    public String activeItemFilterOverride = null;

    public GuideNavigator(String namespace) {
        this.bookNamespace = namespace;
    }

    @SuppressWarnings("unused")
    public GuideNavigator() {
        this(GuideMod.MODID);
    }

    private String buildMdPath(String lang, String fileName) {
        String normalizedLang = lang.toLowerCase().replace("-", "_");
        Minecraft mc = Minecraft.getInstance();

        if (!normalizedLang.equals("en_us")) {
            String localizedPath = "guide/chapters/" + normalizedLang + "/" + fileName + ".md";
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(bookNamespace, localizedPath);

            if (mc.getResourceManager().getResource(loc).isPresent()) {
                return localizedPath;
            }
        }

        return "guide/chapters/" + fileName + ".md";
    }

    private ResourceLocation getChapterLoc(String lang, String fileName) {
        return ResourceLocation.parse(bookNamespace + ":" + buildMdPath(lang, fileName));
    }

    public void loadChaptersFromIndexFile() {
        chapters.clear();
        chapterFileSet.clear();
        submenuParentMap.clear();
        titleCache.clear();
        itemToChapterMap.clear();

        String lang = Minecraft.getInstance().getLanguageManager().getSelected();
        ResourceLocation indexLoc = getChapterLoc(lang, "index");

        try {
            var rm = Minecraft.getInstance().getResourceManager();
            rm.getResource(indexLoc).ifPresent(res -> {
                try (var stream = res.open();
                     var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

                    List<String> tempFiles = new ArrayList<>();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String t = line.trim();
                        if (t.isEmpty() || t.startsWith("#") || t.startsWith("@") || t.equals("index")) continue;

                        String[] parts = t.split("\\|", 2);
                        String fileName = parts[0].trim();
                        String displayName = parts.length > 1 ? parts[1].trim() : fileName;

                        if (chapterFileSet.add(fileName)) {
                            chapters.add(new ChapterInfo(fileName, displayName, false));
                            tempFiles.add(fileName);
                        }
                    }

                    for (String file : tempFiles) {
                        ResourceLocation fileLoc = getChapterLoc(lang, file);
                        rm.getResource(fileLoc).ifPresent(fileRes -> {
                            try (var fStream = fileRes.open();
                                 var fReader = new BufferedReader(new InputStreamReader(fStream, StandardCharsets.UTF_8))) {
                                String fLine;
                                while ((fLine = fReader.readLine()) != null) {
                                    String trimmedLine = fLine.trim();

                                    if (trimmedLine.startsWith("@submenu:")) {
                                        String subs = trimmedLine.replace("@submenu:", "").trim();
                                        for (String sub : subs.split(",")) {
                                            String subTrim = sub.trim().toLowerCase();
                                            if (!subTrim.isEmpty()) {
                                                submenuParentMap.put(subTrim, file);
                                            }
                                        }
                                    }

                                    if (trimmedLine.startsWith("@bind:")) {
                                        String itemsRaw = trimmedLine.replace("@bind:", "").trim();
                                        for (String itemRegistryName : itemsRaw.split(",")) {
                                            String cleanName = itemRegistryName.trim().toLowerCase();
                                            if (!cleanName.isEmpty()) {
                                                itemToChapterMap.computeIfAbsent(cleanName, k -> new ArrayList<>()).add(file);
                                            }
                                        }
                                    }
                                }
                            } catch (Exception ignored) {}
                        });
                    }
                } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("unused")
    public String fetchChapterTitleFromMd(String fileName, boolean isSub) {
        String cacheKey = fileName + "_" + isSub;
        if (titleCache.containsKey(cacheKey)) return titleCache.get(cacheKey);

        String lang = Minecraft.getInstance().getLanguageManager().getSelected();
        String path = buildMdPath(lang, fileName);
        try {
            var res = Minecraft.getInstance().getResourceManager().getResource(ResourceLocation.parse(bookNamespace + ":" + path));
            if (res.isPresent()) {
                try (var reader = new BufferedReader(new InputStreamReader(res.get().open(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String t = line.trim();
                        if (t.startsWith("# ")) {
                            String title = (isSub ? "  • " : "") + t.substring(2).trim();
                            titleCache.put(cacheKey, title);
                            return title;
                        }
                        if (t.startsWith("§b§l")) {
                            String title = (isSub ? "  • " : "") + t.substring(4).trim();
                            titleCache.put(cacheKey, title);
                            return title;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        String fallback = (isSub ? "  • " : "") + fileName;
        titleCache.put(cacheKey, fallback);
        return fallback;
    }

    public boolean navigateToFile(String targetFile) {
        targetFile = targetFile.replace(".md", "").trim().toLowerCase();

        int idx = -1;
        for (int i = 0; i < chapters.size(); i++) {
            if (chapters.get(i).file().equalsIgnoreCase(targetFile)) {
                idx = i;
                break;
            }
        }

        if (idx == -1) {
            return false;
        }

        currentChapterIndex = idx;
        activeSubmenuFileOverride = null;
        parentChapterOfFile = null;

        String parent = submenuParentMap.get(targetFile);
        if (parent != null) {
            parentChapterOfFile = parent;
            activeSubmenuFileOverride = targetFile;
        }

        GuideHistoryManager.addToHistory(targetFile);
        return true;
    }

    public List<String> getChapterFiles() {
        List<String> files = new ArrayList<>();
        for (ChapterInfo info : chapters) {
            files.add(info.file());
        }
        return Collections.unmodifiableList(files);
    }

    public List<String> getChapterNames() {
        List<String> names = new ArrayList<>();
        for (ChapterInfo info : chapters) {
            names.add(info.displayName());
        }
        return Collections.unmodifiableList(names);
    }

    public static Map<String, List<String>> getItemToChapterMap() {
        if (itemToChapterMap.isEmpty()) {
            new GuideNavigator(GuideMod.MODID).loadChaptersFromIndexFile();
        }
        return Collections.unmodifiableMap(itemToChapterMap);
    }

    @SuppressWarnings("unused")
    public List<String> getFavoriteChapters() { return GuideHistoryManager.getFavorites(); }
    public boolean isFavorite(String chapterFile) { return GuideHistoryManager.isFavorite(chapterFile); }
    public void toggleFavorite(String chapterFile) { GuideHistoryManager.toggleFavorite(chapterFile); }

    public static void forceReloadAllData() {
        itemToChapterMap.clear();
        new GuideNavigator(GuideMod.MODID).loadChaptersFromIndexFile();

        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        mc.tell(() -> {
            if (mc.screen instanceof GuideScreen screen) {
                screen.init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
            }
        });
    }
}