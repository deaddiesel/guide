package com.deaddiesel.mods.guide;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GuideHistoryManager {
    private static final List<String> HISTORY = new ArrayList<>();
    private static final Set<String> FAVORITES = new LinkedHashSet<>();
    private static final Path CONFIG_PATH = Path.of("config", "guide", "guide-favorites.properties");

    static {
        loadFavorites();
    }

    public static void loadFavorites() {
        FAVORITES.clear();
        if (!Files.exists(CONFIG_PATH)) return;

        try (var reader = new BufferedReader(new InputStreamReader(Files.newInputStream(CONFIG_PATH), StandardCharsets.UTF_8))) {
            Properties props = new Properties();
            props.load(reader);
            String favorites = props.getProperty("favorites", "");
            if (!favorites.isEmpty()) {
                Arrays.stream(favorites.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(FAVORITES::add);
            }
        } catch (Exception ignored) {}
    }

    public static void saveFavorites() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Properties props = new Properties();
            String favorites = String.join(",", FAVORITES);
            props.setProperty("favorites", favorites);

            try (var writer = new OutputStreamWriter(Files.newOutputStream(CONFIG_PATH), StandardCharsets.UTF_8)) {
                props.store(writer, "Guide Favorites");
            }
        } catch (Exception ignored) {}
    }

    public static void addToHistory(String fileName) {
        if (fileName == null || fileName.isEmpty()) return;
        HISTORY.remove(fileName);
        HISTORY.add(0, fileName);
        if (HISTORY.size() > 10) {
            HISTORY.remove(HISTORY.size() - 1);
        }
    }

    public static List<String> getFavorites() {
        return new ArrayList<>(FAVORITES);
    }

    public static void toggleFavorite(String fileName) {
        if (fileName == null || fileName.isEmpty()) return;
        if (!FAVORITES.add(fileName)) {
            FAVORITES.remove(fileName);
        }
        saveFavorites();
    }

    public static boolean isFavorite(String fileName) {
        return fileName != null && !fileName.isEmpty() && FAVORITES.contains(fileName);
    }
}