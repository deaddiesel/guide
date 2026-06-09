package com.deaddiesel.mods.guide;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public class JeiIntegration {

    public static boolean isManagerLoaded() {
        return ModList.get().isLoaded("jei") || ModList.get().isLoaded("emi") || ModList.get().isLoaded("rei");
    }

    public static void openRecipes(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !isManagerLoaded()) return;

        try {
            JeiPlugin.showRecipesFor(stack);
        } catch (Throwable t) {
            // Logger removed
        }
    }

    public static void openUses(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !isManagerLoaded()) return;

        try {
            JeiPlugin.showUsesFor(stack);
        } catch (Throwable t) {
            // Logger removed
        }
    }
}