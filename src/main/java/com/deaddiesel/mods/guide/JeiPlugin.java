package com.deaddiesel.mods.guide;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

@mezz.jei.api.JeiPlugin
@SuppressWarnings("unused")
public class JeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(GuideMod.MODID, "jei_plugin");

    public static IJeiRuntime jeiRuntime = null;

    @Override
    @Nonnull
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime runtime) {
        jeiRuntime = runtime;
    }

    public static void showRecipesFor(ItemStack stack) {
        if (jeiRuntime != null && stack != null && !stack.isEmpty()) {
            jeiRuntime.getRecipesGui().show(
                    jeiRuntime.getJeiHelpers().getFocusFactory().createFocus(
                            mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT,
                            mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                            stack
                    )
            );
        }
    }

    public static void showUsesFor(ItemStack stack) {
        if (jeiRuntime != null && stack != null && !stack.isEmpty()) {
            jeiRuntime.getRecipesGui().show(
                    jeiRuntime.getJeiHelpers().getFocusFactory().createFocus(
                            mezz.jei.api.recipe.RecipeIngredientRole.INPUT,
                            mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                            stack
                    )
            );
        }
    }
}