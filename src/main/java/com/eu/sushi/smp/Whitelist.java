package com.eu.sushi.smp;


import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.emoji.Emoji;
import discord4j.core.object.entity.Message;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.WhitelistEntry;
import reactor.core.publisher.Mono;

import static com.eu.sushi.smp.Smp.LOGGER;

public class Whitelist {
    public static void initialize(MinecraftServer server) {
        Mono<Void> login = DiscordClient.create(System.getenv("TOKEN")).withGateway((GatewayDiscordClient gateway) ->
                gateway.on(MessageCreateEvent.class, event ->
                        Mono.fromRunnable(() -> {
                            Message message = event.getMessage();
                            if (message.getChannelId().toString().equals("Snowflake{1445523076488888370}")) {
                                String content = message.getContent();
                                if (content.equals(content.strip())) {
                                    net.minecraft.server.Whitelist whitelist = server.getPlayerManager().getWhitelist();
                                    PlayerConfigEntry player = PlayerConfigEntry.fromNickname(content);
                                    LOGGER.info("PlayerConfigEntry: {}", player);
                                    if (whitelist.isAllowed(player)) {
                                        message.delete();
                                        return;
                                    }
                                    WhitelistEntry entry = new WhitelistEntry(player);
                                    LOGGER.info("WhitelistEntry: {}", entry);
                                    whitelist.add(entry);
                                    message.addReaction(Emoji.unicode("✅")).subscribe();
                                } else {
                                    message.delete();
                                }
                            }
                        })
                )
        );
        login.subscribe();
    }
}
