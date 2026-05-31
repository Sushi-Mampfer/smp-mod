package com.eu.sushi.smp;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import discord4j.common.util.Snowflake;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;

import static com.eu.sushi.smp.Smp.MOD_ID;

public class WhitelistSate extends SavedData {
    public record UserUuidPair(Snowflake user, UUID uuid) {
        public static final Codec<UserUuidPair> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                SNOWFLAKE_CODEC.fieldOf("user").forGetter(UserUuidPair::user),
                UUID_CODEC.fieldOf("uuid").forGetter(UserUuidPair::uuid)
        ).apply(instance, UserUuidPair::new));
    }

    private HashMap<Snowflake, Integer> userToWhitelists = new HashMap<>();
    private HashMap<Snowflake, UserUuidPair> messageToData = new HashMap<>();
    private HashMap<Snowflake, UserUuidPair> whitelistRequests = new HashMap<>();
    private List<Snowflake> banList = new ArrayList<>();

    private static final Codec<Snowflake> SNOWFLAKE_CODEC = Codec.STRING.xmap(Snowflake::of, Snowflake::asString);
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);
    private static final Codec<WhitelistSate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(SNOWFLAKE_CODEC, Codec.INT).fieldOf("userToWhitelists").forGetter(WhitelistSate::getUserToWhitelists),
            Codec.unboundedMap(SNOWFLAKE_CODEC, UserUuidPair.CODEC).fieldOf("messageToData").forGetter(WhitelistSate::getMessageToData),
            Codec.unboundedMap(SNOWFLAKE_CODEC, UserUuidPair.CODEC).fieldOf("whitelistRequests").forGetter(WhitelistSate::getWhitelistRequests),
            Codec.list(SNOWFLAKE_CODEC).fieldOf("banList").forGetter(WhitelistSate::getBanList)
    ).apply(instance, WhitelistSate::new));

    private static final SavedDataType<WhitelistSate> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(MOD_ID, "whitelist_data"),
            WhitelistSate::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    public WhitelistSate(Map<Snowflake, Integer> userToWhitelists, Map<Snowflake, UserUuidPair> messageToData, Map<Snowflake, UserUuidPair> whitelistRequests, List<Snowflake> banList) {
        this.userToWhitelists = new HashMap<>(userToWhitelists);
        this.messageToData = new HashMap<>(messageToData);
        this.whitelistRequests = new HashMap<>(whitelistRequests);
        this.banList = new ArrayList<>(banList);
    }

    public static WhitelistSate getWhitelistState(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public WhitelistSate() {}

    private HashMap<Snowflake, Integer> getUserToWhitelists() {
        return userToWhitelists;
    }

    private HashMap<Snowflake, UserUuidPair> getMessageToData() {
        return messageToData;
    }

    private HashMap<Snowflake, UserUuidPair> getWhitelistRequests() {
        return whitelistRequests;
    }

    private List<Snowflake> getBanList() {
        return banList;
    }

    public Integer whitelistCount(Snowflake user) {
        Integer whitelists = userToWhitelists.get(user);
        if (whitelists == null) {
            return 0;
        }
        return whitelists;
    }

    public void pushWhitelist(Snowflake user, Snowflake message, UUID uuid) {
        userToWhitelists.compute(user, (_, v) -> (v == null) ? 1 : v + 1);
        messageToData.put(message, new UserUuidPair(user, uuid));
        setDirty();
    }

    public UserUuidPair popWhitelist(Snowflake message) {
        UserUuidPair data = messageToData.remove(message);
        userToWhitelists.compute(data.user, (_, v) -> (v == null) ? null : v - 1);
        setDirty();
        return data;
    }

    public void pushWhitelistRequest(Snowflake message, Snowflake user, UUID uuid) {
        whitelistRequests.put(message, new UserUuidPair(user, uuid));
        setDirty();
    }

    public UserUuidPair popWhitelistRequest(Snowflake message) {
        UserUuidPair whitelistRequest = whitelistRequests.remove(message);
        setDirty();
        return whitelistRequest;
    }

    public void ban(Snowflake user) {
        this.banList.add(user);
        setDirty();
    }

    public void unban(Snowflake user) {
        this.banList.remove(user);
        setDirty();
    }

    public boolean isBanned(Snowflake user) {
        return this.banList.contains(user);
    }
}