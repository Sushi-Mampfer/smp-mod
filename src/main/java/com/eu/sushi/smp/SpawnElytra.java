package com.eu.sushi.smp;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.*;

import static com.eu.sushi.smp.Smp.LOGGER;

public class SpawnElytra {
    private static final List<UUID> flyingPlayers = new ArrayList<>();
    private static BlockPos spawnPos;
    private static int spawnRadius;

    public static void initialize() {
        spawnPos = new BlockPos(0, 100, 0);
        spawnRadius = 10;
    }

    public static boolean forceGlide(LivingEntity player) {
        if (player instanceof ServerPlayerEntity) {
            return flyingPlayers.contains(player.getUuid());
        }
        return false;
    }

    public static boolean inSpawn(ServerPlayerEntity player) {
        if (player.getEntityWorld().getRegistryKey() != World.OVERWORLD) {
            return false;
        }
        double x = player.getX() - spawnPos.getX();
        double y = player.getY() - spawnPos.getY();
        double z = player.getZ() - spawnPos.getZ();

        double j = MathHelper.absMax(x, y);
        double k = MathHelper.absMax(j, z);

        return k <= spawnRadius;
    }

    public static void addPlayer(ServerPlayerEntity player) {
        flyingPlayers.add(player.getUuid());
    }

    public static void removePlayer(LivingEntity player) {
        if (player instanceof ServerPlayerEntity) {
            flyingPlayers.remove(player.getUuid());
        }
    }
}
