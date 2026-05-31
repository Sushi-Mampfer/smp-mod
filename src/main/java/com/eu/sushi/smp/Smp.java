package com.eu.sushi.smp;

import com.eu.sushi.smp.commands.SmpCommands;
import com.eu.sushi.smp.enchantments.SmpEnchantments;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class Smp implements ModInitializer {
    public static final String MOD_ID = "smp";
    public static ModConfig config;

    @Override
    public void onInitialize() {
        config = ConfigManager.loadConfig();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (config.spawnElytra.enabled) SpawnElytra.initialize(server);
            if (config.whitelist.enabled) Whitelist.initialize(server);
        });

        SmpCommands.initialize();
        if (config.speedyGhast) SmpEnchantments.initialize();
    }
}
