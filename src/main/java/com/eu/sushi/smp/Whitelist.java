package com.eu.sushi.smp;


import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.emoji.Emoji;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateMono;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.MessageReferenceData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.eu.sushi.smp.Smp.LOGGER;

public class Whitelist {
    public static void initialize(MinecraftServer server) {
        Mono<Void> login = DiscordClient.create(System.getenv("TOKEN")).withGateway((GatewayDiscordClient gateway) -> {
            Mono<Void> messageEvent = gateway.on(MessageCreateEvent.class, event ->
                    Mono.fromRunnable(() -> {
                        Message message = event.getMessage();
                        if (message.getChannelId().asString().equals("1445523076488888370")) {
                            if (message.getAuthor().get().getGlobalName().get().equals("BingTschiling")) {
                                LOGGER.info("equal");
                                message.getChannel().block().createMessage(MessageCreateSpec.builder()
                                        .content("Du nid")
                                        .messageReference(MessageReferenceData.builder().messageId(message.getId().asLong()).build())
                                        .build()).block();
                                return;
                            }
                            LOGGER.info("nequal");
                            String content = message.getContent();
                            if (content.equals(content.strip())) {
                                net.minecraft.server.Whitelist whitelist = server.getPlayerManager().getWhitelist();
                                Optional<PlayerConfigEntry> player = server.getApiServices().nameToIdCache().findByName(content);
                                if (player.isEmpty()) {
                                    message.delete().block();
                                    return;
                                }
                                if (whitelist.isAllowed(player.get())) {
                                    message.delete().block();
                                    return;
                                }
                                StateSaverAndLoader state = StateSaverAndLoader.getServerState(server);
                                if (newUser(event.getMember().get().getJoinTime().get().getEpochSecond()) || whitelistCount(event.getMember().get().getId(), state) < 1) {

                                }
                                WhitelistEntry entry = new WhitelistEntry(player.get());
                                whitelist.add(entry);
                                message.addReaction(Emoji.unicode("✅")).block();
                            } else {
                                message.delete().block();
                            }
                        }
                    })
            ).then();
            Mono<Void> reactEvent = gateway.on(ReactionAddEvent.class, event ->
                    Mono.fromRunnable(() -> {

                    })).then();
            return messageEvent.and(reactEvent);
        });
        login.subscribe();
    }

    private static boolean newUser(long timestamp) {
        return Instant.now().getEpochSecond() - timestamp < 24 * 60 * 60 * 30;
    }

    private static int whitelistCount(Snowflake snowflake, StateSaverAndLoader state) {
        List<UUID> list = state.snowflake_to_uuids.get(snowflake);
        if (list == null) {
            return 0;
        }
        return list.toArray().length;
    }
}
