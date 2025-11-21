package com.eu.sushi.smp;

import com.eu.sushi.smp.commands.NoSleep;
import com.eu.sushi.smp.commands.SmpCommands;
import com.eu.sushi.smp.enchantments.SmpEnchantments;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.component.type.EnchantableComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class Smp implements ModInitializer {
    public static final String MOD_ID = "smp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        SmpCommands.initialize();
        SmpEnchantments.initialize();
    }
}
