package com.eu.sushi.smp;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import static com.eu.sushi.smp.Smp.MOD_ID;

public final class SmpEnchantments {
    public static final RegistryKey<Enchantment> SPEEDY_GHAST = of("speedy_ghast");

    private static RegistryKey<Enchantment> of(String name) {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, name));
    }

    public static void initialize() {
    }
}
