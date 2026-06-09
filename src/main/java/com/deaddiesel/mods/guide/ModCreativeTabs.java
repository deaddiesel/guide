package com.deaddiesel.mods.guide;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GuideMod.MODID);

    @SuppressWarnings("unused")
    public static final RegistryObject<CreativeModeTab> IU_GUIDE_TAB = CREATIVE_TABS.register("guide_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.GUIDE_BOOK.orElse(null)))
                    .title(Component.translatable("itemGroup.guide_tab"))
                    .displayItems((parameters, output) -> output.accept(ModItems.GUIDE_BOOK.get()))
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}