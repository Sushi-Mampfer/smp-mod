package com.eu.sushi.smp;

import discord4j.common.util.Snowflake;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class StateSaverAndLoader extends PersistentState {
    public HashMap<Snowflake, List<UUID>> snowflake_to_uuid = new HashMap<>();
    public HashMap<UUID, Snowflake> uuid_to_snowflake = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.put("totalDirtBlocksBroken", totalDirtBlocksBroken);

        NbtCompound playersNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();

            playerNbt.putInt("dirtBlocksBroken", playerData.dirtBlocksBroken);

            playerNbt.putIntArray("oldCravings", playerData.oldCravings);

            NbtCompound fatigueTag = new NbtCompound();
            playerData.fatigue.forEach((foodID, fatigueAmount) -> fatigueTag.putInt(String.valueOf(foodID), fatigueAmount));
            playerNbt.put("fatigue", fatigueTag);

            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);

        return nbt;
    }

    public static StateSaverAndLoader createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        state.totalDirtBlocksBroken = tag.getInt("totalDirtBlocksBroken");

        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerData playerData = new PlayerData();

            playerData.dirtBlocksBroken = playersNbt.getCompound(key).getInt("dirtBlocksBroken");

            NbtCompound fatigueCompound = playersNbt.getCompound(key).getCompound("fatigue");
            fatigueCompound.getKeys().forEach(s -> {
                Integer foodID = Integer.valueOf(s);
                int fatigueAmount = fatigueCompound.getInt(s);
                playerData.fatigue.put(foodID, fatigueAmount);
            });

            for (int oldCravings : playersNbt.getCompound(key).getIntArray("oldCravings")) {
                playerData.oldCravings.add(oldCravings);
            }

            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });

        return state;
    }
}
