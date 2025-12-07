package com.eu.sushi.smp.commands;

import com.eu.sushi.smp.Smp;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import static net.minecraft.server.command.CommandManager.literal;

public class NoSleep {
    private static Text noSleep;

    public static void initialize() {
        registerEvents();
        registerCommand();
    }

    private static void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("nosleep")
                .executes(context -> {
                    Text name = context.getSource().getDisplayName();

                    if (noSleep == null) {
                        noSleep = name;
                        context.getSource().sendFeedback(() -> Text.of("Set no sleep for this night."), false);
                    } else if (noSleep.equals(name)) {
                        noSleep = null;
                        context.getSource().sendFeedback(() -> Text.of("Unset no sleep for this night."), false);
                    } else {
                        context.getSource().sendFeedback(() -> noSleep.copy().append(" has set no sleep for this night."), false);
                    }
                    return 1;
                })
        ));
    }

    private static void registerEvents() {
        EntitySleepEvents.START_SLEEPING.register((entity, pos) -> {
            if (noSleep == null) {
                return;
            }

            if (entity instanceof ServerPlayerEntity player) {
                player.networkHandler.sendPacket(new TitleS2CPacket(Text.of("No Sleep")));
                player.networkHandler.sendPacket(new SubtitleS2CPacket(noSleep.copy().append(" does not want you to sleep.")));
            }
        });
        ServerTickEvents.END_WORLD_TICK.register((world) -> {
            if (world.getRegistryKey() == World.OVERWORLD) {
                if (world.getTimeOfDay() == 4000) {
                    noSleep = null;
                }
            }
        });
    }
}
