package com.eu.sushi.smp;

import com.mojang.datafixers.util.Pair;
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

import static com.eu.sushi.smp.Smp.MOD_ID;

public class StateSaverAndLoader extends PersistentState {
    public HashMap<Snowflake, List<UUID>> snowflake_to_uuids;
    public HashMap<UUID, Snowflake> uuid_to_snowflake;
    public HashMap<Snowflake, Pair<String, Snowflake>> whitelist_requests;

    private static final Codec<Snowflake> SNOWFLAKE_CODEC = Codec.STRING.xmap(Snowflake::of, Snowflake::asString);

    public static final Codec<StateSaverAndLoader> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.unboundedMap(Uuids.CODEC, SNOWFLAKE_CODEC)
                .fieldOf("whitelists")
                .forGetter(state -> state.uuid_to_snowflake)
            ,
            Codec.unboundedMap(SNOWFLAKE_CODEC, Codec.pair(Codec.STRING, SNOWFLAKE_CODEC))
                    .fieldOf("whitelist_requests")
                    .forGetter(state -> state.whitelist_requests)
        ).apply(instance, StateSaverAndLoader::new)
    );

    private StateSaverAndLoader(Map<UUID, Snowflake> uuidSnowflakeMap, Map<Snowflake, Pair<String, Snowflake>> snowflakePairMap) {
        this.uuid_to_snowflake = new HashMap<>();
        this.snowflake_to_uuids = new HashMap<>();
        this.whitelist_requests = new HashMap<>();

        uuidSnowflakeMap.forEach((uuid, snowflake) -> {
            this.uuid_to_snowflake.put(uuid, snowflake);

            if (this.snowflake_to_uuids.containsKey(snowflake)) {
                this.snowflake_to_uuids.get(snowflake).add(uuid);
            } else {
                this.snowflake_to_uuids.put(snowflake, new ArrayList<>());
                this.snowflake_to_uuids.get(snowflake).add(uuid);
            }
        });

        this.whitelist_requests.putAll(snowflakePairMap);
    }

    public StateSaverAndLoader() {
        this.uuid_to_snowflake = new HashMap<>();
        this.snowflake_to_uuids = new HashMap<>();
        this.whitelist_requests = new HashMap<>();
    }

    private static final PersistentStateType<StateSaverAndLoader> TYPE = new PersistentStateType<>(
            MOD_ID,
            StateSaverAndLoader::new,
            CODEC,
            null
    );

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
    }

    public int whitelistCount(Snowflake snowflake) {
        List<UUID> list = this.snowflake_to_uuids.get(snowflake);
        if (list == null) {
            return 0;
        }
        return list.toArray().length;
    }

    public void addWhitelist(Snowflake snowflake, UUID uuid) {
        this.uuid_to_snowflake.put(uuid, snowflake);
        this.snowflake_to_uuids.computeIfAbsent(snowflake, k -> new ArrayList<>());
        this.snowflake_to_uuids.get(snowflake).add(uuid);
        this.markDirty();
    }

    public void removeWhitelist(UUID uuid) {
        Snowflake snowflake = this.uuid_to_snowflake.remove(uuid);
        this.snowflake_to_uuids.get(snowflake).remove(uuid);
        this.markDirty();
    }

    public void addWhitelistRequest(Snowflake message, String name, Snowflake user) {
        this.whitelist_requests.put(message, new Pair<>(name, user));
        this.markDirty();
    }

    public Pair<String, Snowflake> popWhitelistRequest(Snowflake message) {
        this.markDirty();
        return this.whitelist_requests.remove(message);
    }
}
