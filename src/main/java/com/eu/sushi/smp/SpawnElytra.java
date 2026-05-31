package com.eu.sushi.smp;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import java.util.*;


public class SpawnElytra {
    private static final List<UUID> flyingPlayers = new ArrayList<>();
    private static BlockPos spawnPos;
    private static int spawnRadius;

    public static void initialize(MinecraftServer server) {
        spawnPos = server.overworld().getRespawnData().pos();
        spawnRadius = Smp.config.spawnElytra.radius;
    }

    public static boolean forceGlide(LivingEntity player) {
        if (player instanceof ServerPlayer) {
            return flyingPlayers.contains(player.getUUID());
        }
        return false;
    }

    public static boolean inSpawn(ServerPlayer player) {
        if (player.level().dimension() != Level.OVERWORLD) {
            return false;
        }
        double x = player.getX() - spawnPos.getX();
        double y = player.getY() - spawnPos.getY();
        double z = player.getZ() - spawnPos.getZ();

        double j = Mth.absMax(x, y);
        double k = Mth.absMax(j, z);

        return k <= spawnRadius;
    }

    public static void addPlayer(ServerPlayer player) {
        flyingPlayers.add(player.getUUID());
    }

    public static void removePlayer(LivingEntity player) {
        if (player instanceof ServerPlayer) {
            flyingPlayers.remove(player.getUUID());
        }
    }
}
