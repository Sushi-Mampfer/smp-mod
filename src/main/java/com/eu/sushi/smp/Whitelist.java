package com.eu.sushi.smp;


import com.mojang.datafixers.util.Pair;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.emoji.Emoji;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.MessageReferenceData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.WhitelistEntry;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;

import static com.eu.sushi.smp.Smp.LOGGER;

public class Whitelist {
    static private final Snowflake CHANNEL = Snowflake.of("1445523076488888370");
    static private final Snowflake WHITELIST_PREMIUM = Snowflake.of("1446085376417464413");
    static private final Snowflake WEBHOOK = Snowflake.of("1446067399618203664");

    public static void initialize(MinecraftServer server) {
        Mono<Void> login = DiscordClient.create(System.getenv("TOKEN")).withGateway((GatewayDiscordClient gateway) -> {
            Mono<Void> messageEvent = gateway.on(MessageCreateEvent.class, event ->
                    Mono.fromRunnable(() -> {
                        Message message = event.getMessage();
                        if (message.getChannelId().equals(CHANNEL)) {
                            if (message.getAuthor().isEmpty()) {
                                return;
                            }
                            if (message.getAuthor().get().isBot()) {
                                return;
                            }
                            if (message.getAuthorAsMember().block().getDisplayName().equals("Odoardo")) {
                                message.getChannel().block().createMessage(MessageCreateSpec.builder()
                                        .content("Du nid")
                                        .messageReference(MessageReferenceData.builder().messageId(message.getId().asLong()).build())
                                        .build()).block();
                                return;
                            }
                            if (message.getContent().equalsIgnoreCase("ghostinator")) {
                                message.getChannel().block().createMessage(MessageCreateSpec.builder()
                                        .content("Dä nid")
                                        .messageReference(MessageReferenceData.builder().messageId(message.getId().asLong()).build())
                                        .build()).block();
                                return;
                            }
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
                                if ((newUser(event.getMember().get().getJoinTime().get().getEpochSecond()) || state.whitelistCount(event.getMember().get().getId()) > 0) && event.getMember().get().getRoleIds().contains(WHITELIST_PREMIUM)) {
                                    message.delete().block();
                                    Message msg = gateway.getWebhookById(WEBHOOK).flatMap(webhook -> webhook
                                            .execute().withContent("Please wait for admin verification.\n" + content)
                                            .withUsername(event.getMember().get().getDisplayName())
                                            .withAvatarUrl(event.getMember().get().getAvatarUrl())
                                            .withWaitForMessage(true)
                                    ).block();
                                    if (msg == null) {
                                        return;
                                    }
                                    msg.addReaction(Emoji.unicode("✅")).block();
                                    msg.addReaction(Emoji.unicode("❌")).block();
                                    state.addWhitelistRequest(msg.getId(), content, event.getMember().get().getId());
                                    return;
                                }
                                message.delete().block();
                                Message msg = gateway.getWebhookById(WEBHOOK).flatMap(webhook -> webhook
                                        .execute().withContent(content)
                                        .withUsername(event.getMember().get().getDisplayName())
                                        .withAvatarUrl(event.getMember().get().getAvatarUrl())
                                        .withWaitForMessage(true)
                                ).block();
                                if (msg == null) {
                                    return;
                                }
                                WhitelistEntry entry = new WhitelistEntry(player.get());
                                state.addWhitelist(event.getMember().get().getId(), player.get().id());
                                whitelist.add(entry);
                                msg.addReaction(Emoji.unicode("✅")).block();
                            } else {
                                message.delete().block();
                            }
                        }
                    })
            ).then();
            Mono<Void> reactEvent = gateway.on(ReactionAddEvent.class, event ->
                    Mono.fromRunnable(() -> {
                        if (event.getChannelId().equals(CHANNEL)) {
                            Message message = event.getMessage().block();
                            Emoji emoji = event.getEmoji();
                            StateSaverAndLoader state = StateSaverAndLoader.getServerState(server);
                            if (emoji.equals(Emoji.unicode("✅"))) {
                                if (event.getMember().get().getRoleIds().contains(WHITELIST_PREMIUM)) {
                                    Pair<String, Snowflake> pair = state.popWhitelistRequest(event.getMessageId());
                                    if (pair == null) {
                                        message.delete();
                                        return;
                                    }
                                    net.minecraft.server.Whitelist whitelist = server.getPlayerManager().getWhitelist();
                                    Optional<PlayerConfigEntry> player = server.getApiServices().nameToIdCache().findByName(pair.getFirst());
                                    if (player.isEmpty()) {
                                        message.delete().block();
                                        return;
                                    }
                                    if (whitelist.isAllowed(player.get())) {
                                        message.delete().block();
                                        return;
                                    }
                                    state.addWhitelist(pair.getSecond(), player.get().id());
                                    message.removeAllReactions().block();
                                    message.addReaction(Emoji.unicode("✅")).block();
                                    gateway.getWebhookById(WEBHOOK).flatMap(webhook -> webhook
                                            .editMessage(event.getMessageId())
                                            .withContent(pair.getFirst())
                                    ).block();
                                }
                            } else if (event.getEmoji().equals(Emoji.unicode("❌"))) {
                                if (event.getMember().get().getRoleIds().contains(WHITELIST_PREMIUM)) {
                                    String content = message.getContent();
                                    message.delete().block();
                                    if (state.popWhitelistRequest(event.getMessageId()) != null) {
                                        message.delete().block();
                                        return;
                                    }
                                    Optional<PlayerConfigEntry> player = server.getApiServices().nameToIdCache().findByName(content);
                                    if (player.isEmpty()) {
                                        return;
                                    }
                                    PlayerManager playerManager = server.getPlayerManager();
                                    playerManager.remove(playerManager.getPlayer(player.get().id()));
                                    net.minecraft.server.Whitelist whitelist = playerManager.getWhitelist();
                                    whitelist.remove(player.get());
                                    state.removeWhitelist(player.get().id());

                                }
                            }
                        }
                    })).then();
            return messageEvent.and(reactEvent);
        });
        login.subscribe();
    }

    private static boolean newUser(long timestamp) {
        return Instant.now().getEpochSecond() - timestamp < 24 * 60 * 60 * 30;
    }
}
