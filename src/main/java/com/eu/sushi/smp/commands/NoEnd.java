package com.eu.sushi.smp.commands;

import com.eu.sushi.smp.ConfigManager;
import com.eu.sushi.smp.Smp;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;

public class NoEnd {
    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("toggleend")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.argument("enabled", BoolArgumentType.bool()).executes(context -> {
                    final boolean end = BoolArgumentType.getBool(context, "enabled");
                    Smp.config.noEnd = !end;
                    ConfigManager.saveConfig(Smp.config);

                    return 1;
                }))
        ));
    }
}
