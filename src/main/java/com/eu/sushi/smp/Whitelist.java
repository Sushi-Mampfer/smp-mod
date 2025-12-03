package com.eu.sushi.smp;


import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.emoji.Emoji;
import discord4j.core.object.entity.Message;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.WhitelistEntry;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;

import static com.eu.sushi.smp.Smp.LOGGER;

public class Whitelist {
    public static void initialize(MinecraftServer server) {
        Mono<Void> login = DiscordClient.create(System.getenv("TOKEN")).withGateway((GatewayDiscordClient gateway) -> {
            Mono<Void> messageEvent = gateway.on(MessageCreateEvent.class, event ->
                    Mono.fromRunnable(() -> {
                        Message message = event.getMessage();
                        if (message.getChannelId().toString().equals("Snowflake{1445523076488888370}")) {
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
                                if (Instant.now().getEpochSecond() - event.getMember().get().getJoinTime().get().getEpochSecond() < 24 * 60 * 60 * 30 || ) {

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
}
