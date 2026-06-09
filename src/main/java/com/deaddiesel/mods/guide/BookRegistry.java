package com.deaddiesel.mods.guide;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class BookRegistry {

    private static final List<BookEntry> registry = new ArrayList<>();

    public record BookEntry(String nameKey, String namespace, String defaultChapter, String iconPath, boolean isDevOnly) {
        public Component getDisplayName() {
            return Component.translatable(nameKey);
        }
    }

    public static List<BookEntry> getVisibleBooks() {
        if (registry.isEmpty()) {
            scanBooks();
        }
        return registry;
    }

    public static void scanBooks() {
        registry.clear();

        Minecraft mc = Minecraft.getInstance();

        var resources = mc.getResourceManager().listResources("guide", path -> path.getPath().endsWith("book.json"));

        for (var entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();
            Resource resource = entry.getValue();

            String bookNamespace = id.getNamespace();

            try (InputStreamReader reader = new InputStreamReader(resource.open())) {
                JsonElement json = JsonParser.parseReader(reader);
                if (json == null || !json.isJsonObject()) continue;

                var root = json.getAsJsonObject();

                String nameKey = GsonHelper.getAsString(root, "name");
                boolean isDev = GsonHelper.getAsBoolean(root, "dev_only", false);
                String icon = GsonHelper.getAsString(root, "icon", "");
                String defaultChapter = GsonHelper.getAsString(root, "default_chapter", "");

                if (isDev && !GuideConfig.SHOW_DEV_MANUAL.get()) {
                    continue;
                }

                registry.add(new BookEntry(nameKey, bookNamespace, defaultChapter, icon, isDev));

            } catch (Exception e) {
                GuideMod.LOGGER.error("Failed to load book from namespace '{}': {}", bookNamespace, e.getMessage());
            }
        }
    }

    @SuppressWarnings("unused")
    public static void refresh() {
        registry.clear();
    }
}