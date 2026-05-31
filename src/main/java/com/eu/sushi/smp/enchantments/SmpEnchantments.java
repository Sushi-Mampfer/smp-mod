package com.eu.sushi.smp.enchantments;

import static com.eu.sushi.smp.Smp.MOD_ID;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public final class SmpEnchantments {
    public static final ResourceKey<Enchantment> SPEEDY_GHAST = of("speedy_ghast");

    private static ResourceKey<Enchantment> of(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(MOD_ID, name));
    }

    public static void initialize() {
        SpeedyGhast.initialize();
    }
}
