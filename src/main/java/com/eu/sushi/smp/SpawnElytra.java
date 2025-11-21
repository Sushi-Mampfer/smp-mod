package com.eu.sushi.smp;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpawnElytra {
    private static final Map<UUID, ItemStack> flyingPlayerItems = new HashMap<UUID, ItemStack>();


    private static void setPlayerItem(ServerPlayerEntity player) {
        player.getInventory();
    }
}
