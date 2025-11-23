package com.eu.sushi.smp;

import com.eu.sushi.smp.commands.SmpCommands;
import com.eu.sushi.smp.enchantments.SmpEnchantments;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class Smp implements ModInitializer {
    public static final String MOD_ID = "smp";

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> SpawnElytra.initialize(server));

        SmpCommands.initialize();
        SmpEnchantments.initialize();
    }
}
