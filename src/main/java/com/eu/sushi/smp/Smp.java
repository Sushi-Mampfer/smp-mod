package com.eu.sushi.smp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.component.type.EnchantableComponent;
import net.minecraft.registry.tag.ItemTags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Smp implements ModInitializer {
    public static final String MOD_ID = "smp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Collection<Item> HARNESSES = List.of(
            Items.WHITE_HARNESS,
            Items.ORANGE_HARNESS,
            Items.MAGENTA_HARNESS,
            Items.LIGHT_BLUE_HARNESS,
            Items.YELLOW_HARNESS,
            Items.LIME_HARNESS,
            Items.PINK_HARNESS,
            Items.GRAY_HARNESS,
            Items.LIGHT_GRAY_HARNESS,
            Items.CYAN_HARNESS,
            Items.PURPLE_HARNESS,
            Items.BLUE_HARNESS,
            Items.BROWN_HARNESS,
            Items.GREEN_HARNESS,
            Items.RED_HARNESS,
            Items.BLACK_HARNESS
    );

    @Override
    public void onInitialize() {
        DefaultItemComponentEvents.MODIFY.register((context) -> {
            context.modify(HARNESSES, (builder, _item) -> {builder.add(DataComponentTypes.ENCHANTABLE, new EnchantableComponent(1));});
        });
        SmpEnchantments.initialize();
    }
}
