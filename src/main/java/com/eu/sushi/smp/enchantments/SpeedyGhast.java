package com.eu.sushi.smp.enchantments;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantable;
import java.util.Collection;
import java.util.List;

public class SpeedyGhast {
    private static final Collection<Item> HARNESSES = List.of(
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

    public static void initialize() {
        registerEvent();
    }
    private static void registerEvent() {
        DefaultItemComponentEvents.MODIFY.register((context) -> context.modify(HARNESSES, (builder, _) -> builder.set(DataComponents.ENCHANTABLE, new Enchantable(1)).set(DataComponents.DAMAGE, 0).set(DataComponents.MAX_DAMAGE, 1)));
    }
}
