package com.deaddiesel.mods.guide;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = GuideMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModKeyMappings {

    public static final KeyMapping OPEN_GUIDE_FOR_ITEM = new KeyMapping(
            "key.guide.open_for_item",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.guide"
    );

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_GUIDE_FOR_ITEM);
    }

    public static boolean isHoldingKey() {
        if (Minecraft.getInstance().screen == null) return false;
        long windowHandle = Minecraft.getInstance().getWindow().getWindow();
        int keyCode = OPEN_GUIDE_FOR_ITEM.getKey().getValue();
        if (keyCode == InputConstants.UNKNOWN.getValue()) return false;

        return GLFW.glfwGetKey(windowHandle, keyCode) == GLFW.GLFW_PRESS;
    }
}