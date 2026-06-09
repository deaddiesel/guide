package com.deaddiesel.mods.guide;

import net.minecraft.resources.ResourceLocation;
import java.util.Objects;

public class GuideModels {

    public record ClickableTextLine(int x, int y, int width, boolean isLink, String targetFile) {}
    public record ClickableItemZone(int x, int y, int width, int height, net.minecraft.world.item.ItemStack stack) {}

    public record StructureTabEntry(ResourceLocation location, String tabName, boolean hasCustomName) {
        public StructureTabEntry {
            Objects.requireNonNull(location, "Structure location cannot be null");
            if (tabName == null) tabName = "";
        }
    }
}