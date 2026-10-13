package com.eu.sushi.smp.enchantments;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantable;
import java.util.Collection;
import java.util.List;

public class SpeedyGhast {

    public static void initialize() {
        registerEvent();
    }
    private static void registerEvent() {
        DefaultItemComponentEvents.MODIFY.register((context) -> context.modify(Items.HARNESS.asList(), (builder, _) -> builder.set(DataComponents.ENCHANTABLE, new Enchantable(1)).set(DataComponents.DAMAGE, 0).set(DataComponents.MAX_DAMAGE, 1)));
    }
}
