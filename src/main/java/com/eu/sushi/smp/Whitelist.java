package com.eu.sushi.smp;


import com.mojang.datafixers.util.Pair;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.emoji.Emoji;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.MessageReferenceData;
import discord4j.discordjson.possible.Possible;
import net.minecraft.server.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;

public class Whitelist {
    static private String TOKEN;
    static private Snowflake CHANNEL;
    static private Snowflake ROLE;
    static private Snowflake WEBHOOK;

    public static void initialize(MinecraftServer server) {
        TOKEN = Smp.config.whitelist.token;
        CHANNEL = Snowflake.of(Smp.config.whitelist.channel);
        ROLE = Snowflake.of(Smp.config.whitelist.whitelist_role);
        WEBHOOK = Snowflake.of(Smp.config.whitelist.webhook_id);

        Mono<Void> login = DiscordClient.create(TOKEN).withGateway((GatewayDiscordClient gateway) -> {
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

                            MessageChannel channel = message.getChannel().block();
                            if (channel == null) return;

                            Member author = message.getAuthorAsMember().block();
                            if (author == null) return;

                            if (author.getDisplayName().equals("Odoardo")) {
                                channel.createMessage(MessageCreateSpec.builder()
                                        .content("Du nid")
                                        .messageReference(MessageReferenceData.builder().messageId(message.getId().asLong()).build())
                                        .build()).block();
                                return;
                            }
                            if (message.getContent().equalsIgnoreCase("ghostinator")) {
                                channel.createMessage(MessageCreateSpec.builder()
                                        .content("Dä nid")
                                        .messageReference(MessageReferenceData.builder().messageId(message.getId().asLong()).build())
                                        .build()).block();
                                return;
                            }
                            String content = message.getContent();
                            message.delete().block();
                            if (content.equals(content.strip())) {
                                PlayerManager playerManager = server.getPlayerManager();

                                net.minecraft.server.Whitelist whitelist = playerManager.getWhitelist();
                                BannedPlayerList banlist = playerManager.getUserBanList();

                                PlayerConfigEntry player = server.getApiServices().nameToIdCache().findByName(content).orElse(null);
                                if (player == null) return;
                                if (whitelist.isAllowed(player)) return;
                                StateSaverAndLoader state = StateSaverAndLoader.getServerState(server);

                                Instant joinTime = author.getJoinTime().orElse(null);
                                if (joinTime == null) return;

                                boolean banned = banlist.contains(player);

                                if (banned && !Smp.config.whitelist.verification) return;

                                if ((newUser(joinTime.getEpochSecond()) || state.whitelistCount(author.getId()) > 0 || banned) && !author.getRoleIds().contains(ROLE) && Smp.config.whitelist.verification) {
                                    Message msg = gateway.getWebhookById(WEBHOOK).flatMap(webhook -> webhook
                                            .execute().withContent("**" + content + "**\n\nPlease wait for <@&" + ROLE.asString() + (banned ? "> unban." : "> verification."))
                                            .withUsername(author.getDisplayName())
                                            .withAvatarUrl(author.getAvatarUrl())
                                            .withWaitForMessage(true)
                                    ).block();
                                    if (msg == null) {
                                        return;
                                    }
                                    msg.addReaction(Emoji.unicode("✅")).block();
                                    msg.addReaction(Emoji.unicode("❌")).block();
                                    state.addWhitelistRequest(msg.getId(), content, author.getId());
                                    return;
                                }

                                Message msg = gateway.getWebhookById(WEBHOOK).flatMap(webhook -> webhook
                                        .execute().withContent(content)
                                        .withUsername(author.getDisplayName())
                                        .withAvatarUrl(author.getAvatarUrl())
                                        .withWaitForMessage(true)
                                ).block();

                                if (msg == null) return;

                                if (banlist.contains(player)) banlist.remove(player);

                                WhitelistEntry entry = new WhitelistEntry(player);
                                state.addWhitelist(author.getId(), player.id());
                                whitelist.add(entry);
                                msg.addReaction(Emoji.unicode("✅")).block();
                            }
                        }
                    })
            ).then();
            Mono<Void> reactEvent = gateway.on(ReactionAddEvent.class, event ->
                    Mono.fromRunnable(() -> {
                        if (event.getChannelId().equals(CHANNEL)) {
                            Message message = event.getMessage().block();
                            if (message == null) return;

                            Member member = event.getMember().orElse(null);
                            if (member == null) return;

                            Emoji emoji = event.getEmoji();
                            StateSaverAndLoader state = StateSaverAndLoader.getServerState(server);

                            if (emoji.equals(Emoji.unicode("✅"))) {
                                if (member.getRoleIds().contains(ROLE)) {
                                    Pair<String, Snowflake> pair = state.popWhitelistRequest(event.getMessageId());
                                    if (pair == null) {
                                        return;
                                    }

                                    PlayerManager playerManager = server.getPlayerManager();
                                    net.minecraft.server.Whitelist whitelist = playerManager.getWhitelist();
                                    BannedPlayerList banlist = playerManager.getUserBanList();

                                    PlayerConfigEntry player = server.getApiServices().nameToIdCache().findByName(pair.getFirst()).orElse(null);
                                    if (player == null) {
                                        message.delete().block();
                                        return;
                                    }

                                    if (whitelist.isAllowed(player)) {
                                        message.delete().block();
                                        return;
                                    }

                                    if (banlist.contains(player)) banlist.remove(player);

                                    WhitelistEntry entry = new WhitelistEntry(player);
                                    whitelist.add(entry);

                                    state.addWhitelist(pair.getSecond(), player.id());
                                    message.removeAllReactions().block();
                                    message.addReaction(Emoji.unicode("✅")).block();
                                    gateway.getWebhookById(WEBHOOK).flatMap(webhook -> webhook
                                            .editMessage(event.getMessageId())
                                            .withContent(Possible.of(Optional.of(pair.getFirst())))
                                    ).block();
                                }
                            } else if (event.getEmoji().equals(Emoji.unicode("❌"))) {
                                if (member.getRoleIds().contains(ROLE)) {
                                    String content = message.getContent();
                                    message.delete().block();

                                    if (state.popWhitelistRequest(event.getMessageId()) != null) return;

                                    PlayerConfigEntry player = server.getApiServices().nameToIdCache().findByName(content).orElse(null);
                                    if (player == null) return;

                                    PlayerManager playerManager = server.getPlayerManager();

                                    net.minecraft.server.Whitelist whitelist = playerManager.getWhitelist();
                                    whitelist.remove(player);
                                    state.removeWhitelist(player.id());

                                    ServerPlayerEntity serverPlayerEntity = playerManager.getPlayer(player.id());
                                    if (serverPlayerEntity == null) return;
                                    serverPlayerEntity.networkHandler.disconnect(Text.translatable("multiplayer.disconnect.not_whitelisted"));
                                }
                            } else if (event.getEmoji().equals(Emoji.unicode("⛔"))) {
                                if (member.getRoleIds().contains(ROLE)) {
                                    String content = message.getContent();
                                    message.delete().block();

                                    PlayerConfigEntry player = server.getApiServices().nameToIdCache().findByName(content).orElse(null);

                                    if (player == null) return;

                                    PlayerManager playerManager = server.getPlayerManager();

                                    BannedPlayerList banList = playerManager.getUserBanList();
                                    BannedPlayerEntry bannedPlayerEntry = new BannedPlayerEntry(player);
                                    banList.add(bannedPlayerEntry);

                                    net.minecraft.server.Whitelist whitelist = playerManager.getWhitelist();
                                    whitelist.remove(player);
                                    state.removeWhitelist(player.id());

                                    ServerPlayerEntity serverPlayerEntity = playerManager.getPlayer(player.id());
                                    if (serverPlayerEntity == null) return;
                                    serverPlayerEntity.networkHandler.disconnect(Text.translatable("multiplayer.disconnect.banned"));
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
