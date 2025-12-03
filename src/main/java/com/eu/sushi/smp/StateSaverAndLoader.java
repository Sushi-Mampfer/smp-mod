package com.eu.sushi.smp;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import discord4j.common.util.Snowflake;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.*;
import java.util.stream.Collectors;

import static com.eu.sushi.smp.Smp.MOD_ID;

public class StateSaverAndLoader extends PersistentState {
    public HashMap<Snowflake, List<UUID>> snowflake_to_uuids;
    public HashMap<UUID, Snowflake> uuid_to_snowflake;

    public static final Codec<StateSaverAndLoader> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.unboundedMap(Uuids.CODEC, Codec.STRING)
                .fieldOf("whitelists")
                .forGetter(state ->
                        state.uuid_to_snowflake.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().asString()))
                )
        ).apply(instance, StateSaverAndLoader::new)
    );

    private StateSaverAndLoader(Map<UUID, String> uuidToSnowflakeStrings) {
        this.uuid_to_snowflake = new HashMap<>();
        this.snowflake_to_uuids = new HashMap<>();

        uuidToSnowflakeStrings.forEach((uuid, snowflake) -> {
            Snowflake snowflake1 = Snowflake.of(snowflake);
            this.uuid_to_snowflake.put(uuid, snowflake1);

            if (this.snowflake_to_uuids.containsKey(snowflake1)) {
                this.snowflake_to_uuids.get(snowflake1).add(uuid);
            } else {
                this.snowflake_to_uuids.put(snowflake1, new ArrayList<>());
                this.snowflake_to_uuids.get(snowflake1).add(uuid);
            }
        });
    }

    public StateSaverAndLoader() {
        this.uuid_to_snowflake = new HashMap<>();
        this.snowflake_to_uuids = new HashMap<>();
    }

    private static PersistentStateType<StateSaverAndLoader> TYPE = new PersistentStateType<>(
            Identifier.of(MOD_ID, "whitelist").toString(),
            StateSaverAndLoader::new,
            CODEC,
            null
    );

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
    }
}
