package com.deaddiesel.mods.guide;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, GuideMod.MODID);

    public static final RegistryObject<Item> GUIDE_BOOK = ITEMS.register("guide_book", GuideBookItem::new);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static class GuideBookItem extends Item {
        public GuideBookItem() {
            super(new Item.Properties().stacksTo(1).fireResistant());
        }

        @Override
        @Nonnull
        public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
            if (level.isClientSide()) {
                var mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.screen == null) {
                    mc.setScreen(new BookSelectorScreen());
                    player.playSound(SoundEvents.UI_BUTTON_CLICK.get(), 0.4f, 1.5f);
                }
            }
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
    }
}