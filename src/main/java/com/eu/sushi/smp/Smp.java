package com.eu.sushi.smp;

import com.eu.sushi.smp.commands.SmpCommands;
import com.eu.sushi.smp.enchantments.SmpEnchantments;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Smp implements ModInitializer {
    public static final String MOD_ID = "smp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        SpawnElytra.initialize();
        SmpCommands.initialize();
        SmpEnchantments.initialize();
    }
}
