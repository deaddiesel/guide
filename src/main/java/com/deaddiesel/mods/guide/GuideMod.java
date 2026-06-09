package com.deaddiesel.mods.guide;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraftforge.fml.config.ModConfig;

@Mod(GuideMod.MODID)
public class GuideMod {
    @SuppressWarnings("SpellCheckingInspection")
    public static final String MODID = "guide";
    public static final Logger LOGGER = LoggerFactory.getLogger(GuideMod.class);

    public GuideMod(FMLJavaModLoadingContext context) {
        var modEventBus = context.getModEventBus();
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(PlacementProjector.class);

        context.registerConfig(ModConfig.Type.CLIENT, GuideConfig.SPEC, "guide/guide-client.toml");

        LOGGER.info("Guide loaded!");
    }

    @SuppressWarnings("unused")
    @EventBusSubscriber(modid = MODID, bus = Bus.FORGE, value = Dist.CLIENT)
    public static class ClientEvents {

        private static int holdingTicks = 0;
        private static final int MAX_SCAN_TICKS = 30;
        private static String lastHoveredItem = "";

        @SubscribeEvent
        public static void onRenderHand(RenderHandEvent event) {
            if (Minecraft.getInstance().screen instanceof GuideScreen) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
            if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> containerScreen) {

                if (ModKeyMappings.isHoldingKey() && !lastHoveredItem.isEmpty()) {
                    holdingTicks++;

                    if (holdingTicks >= MAX_SCAN_TICKS) {
                        holdingTicks = 0;
                        String itemToOpen = lastHoveredItem;
                        lastHoveredItem = "";

                        java.util.List<String> targetChapters = GuideNavigator.getItemToChapterMap().get(itemToOpen);
                        if (targetChapters != null && !targetChapters.isEmpty()) {
                            mc.tell(() -> {
                                GuideScreen guideScreen = new GuideScreen();
                                mc.setScreen(guideScreen);

                                if (mc.screen instanceof GuideScreen currentScreen) {
                                    var nav = currentScreen.getNavigator();
                                    if (targetChapters.size() == 1) {
                                        nav.activeItemFilterOverride = null;
                                        nav.navigateToFile(targetChapters.get(0));
                                    } else {
                                        nav.activeItemFilterOverride = itemToOpen;
                                        nav.navigateToFile(targetChapters.get(0));
                                    }
                                    currentScreen.init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
                                }
                            });
                        }
                    }
                } else {
                    holdingTicks = 0;
                }
            } else {
                holdingTicks = 0;
            }
        }

        @SubscribeEvent
        public static void onScreenKeyPressed(net.minecraftforge.client.event.ScreenEvent.KeyPressed.Pre event) {
            Minecraft mc = Minecraft.getInstance();
            var key = com.mojang.blaze3d.platform.InputConstants.getKey(event.getKeyCode(), event.getScanCode());
            if (ModKeyMappings.OPEN_GUIDE_FOR_ITEM.isActiveAndMatches(key) && !lastHoveredItem.isEmpty()) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onItemTooltip(net.minecraftforge.event.entity.player.ItemTooltipEvent event) {
            if (event.getItemStack().isEmpty()) return;

            net.minecraft.resources.ResourceLocation itemKey = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
            if (itemKey == null) return;

            String itemRegistryName = itemKey.toString().toLowerCase();

            if (GuideNavigator.getItemToChapterMap().containsKey(itemRegistryName)) {
                lastHoveredItem = itemRegistryName;

                event.getToolTip().add(Component.translatable("guide.tooltip.press_g"));

                if (ModKeyMappings.isHoldingKey() && holdingTicks > 0) {
                    int totalBars = 80;
                    int progress = (int) (((double) holdingTicks / MAX_SCAN_TICKS) * totalBars);
                    int percent = (int) (((double) holdingTicks / MAX_SCAN_TICKS) * 100);

                    StringBuilder bar = new StringBuilder(" §7［§6");
                    for (int i = 0; i < totalBars; i++) {
                        if (i < progress) {
                            bar.append("┃");
                        } else {
                            bar.append("§8┃§6");
                        }
                    }

                    bar.append("§7］ §7［§a").append(percent).append("%§7］");

                    event.getToolTip().add(Component.literal(bar.toString()));
                }

            }
        }
    }
}