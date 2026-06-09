package com.deaddiesel.mods.guide;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = GuideMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("guide")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("reload")
                                .executes(context -> {
                                    GuideNavigator.forceReloadAllData();

                                    context.getSource().sendSuccess(() ->
                                                    Component.translatable("guide.command.reload.success"),
                                            true
                                    );
                                    return 1;
                                })
                        )
        );
    }
}