package com.eu.sushi.smp;

import com.eu.sushi.smp.commands.SmpCommands;
import com.eu.sushi.smp.enchantments.SmpEnchantments;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Smp implements ModInitializer {
    public static final String MOD_ID = "smp";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
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
