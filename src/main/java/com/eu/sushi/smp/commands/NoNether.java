package com.eu.sushi.smp.commands;

import com.eu.sushi.smp.ConfigManager;
import com.eu.sushi.smp.Smp;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;

public class NoNether {
    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> dispatcher.register(Commands.literal("togglenether")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
                .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(context -> {
                    final boolean nether = BoolArgumentType.getBool(context, "enabled");
                    Smp.config.noNether = !nether;
                    ConfigManager.saveConfig(Smp.config);

                    return 1;
                }))
        ));
    }
}
