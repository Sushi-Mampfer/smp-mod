package com.eu.sushi.smp;

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
import discord4j.core.spec.WebhookMessageEditSpec;
import discord4j.discordjson.json.MessageReferenceData;
import discord4j.discordjson.possible.Possible;
import net.minecraft.network.chat.Component;
import net.minecraft.server.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;

public class Whitelist {
    static private Snowflake CHANNEL;
    static private Snowflake ROLE;
    static private Snowflake WEBHOOK;

    public static void initialize(MinecraftServer server) {
        String TOKEN = Smp.config.whitelist.token;
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

                            String content = message.getContent().strip();

                            if (content.equalsIgnoreCase("ghostinator")) {
                                channel.createMessage(MessageCreateSpec.builder()
                                        .content("Dä nid")
                                        .messageReference(MessageReferenceData.builder().messageId(message.getId().asLong()).build())
                                        .build()).block();
                                return;
                            }
                            message.delete().block();
                            if (!content.contains(" ")) {
                                PlayerList playerManager = server.getPlayerList();

                                net.minecraft.server.players.UserWhiteList whitelist = playerManager.getWhiteList();
                                UserBanList banlist = playerManager.getBans();

                                NameAndId player = server.services().nameToIdCache().get(content).orElse(null);
                                if (player == null) return;
                                if (whitelist.isWhiteListed(player)) return;

                                WhitelistSate state = WhitelistSate.getWhitelistState(server);

                                Instant joinTime = author.getJoinTime().orElse(null);
                                if (joinTime == null) return;

                                boolean banned = banlist.isBanned(player) || state.isBanned(author.getId());

                                if (banned && !Smp.config.whitelist.verification) return;

                                if ((newUser(joinTime.getEpochSecond()) || state.whitelistCount(author.getId()) > 0 || banned) && !author.getRoleIds().contains(ROLE) && Smp.config.whitelist.verification) {
                                    Message msg = gateway.getWebhookById(WEBHOOK).flatMap(webhook -> webhook
                                            .execute().withContent("**" + content + "**\n\n" + (banned ? ("You are banned!\nPlease wait for <@&" + ROLE.asString() + "> to review your unban request.") : ("Please wait for <@&" + ROLE.asString() + "> verification.")))
                                            .withUsername(author.getDisplayName())
                                            .withAvatarUrl(author.getAvatarUrl())
                                            .withWaitForMessage(true)
                                    ).block();
                                    if (msg == null) {
                                        return;
                                    }
                                    msg.addReaction(Emoji.unicode("✅")).block();
                                    msg.addReaction(Emoji.unicode("❌")).block();
                                    state.pushWhitelistRequest(msg.getId(), author.getId(), player.id());
                                    return;
                                }

                                Message msg = gateway.getWebhookById(WEBHOOK).flatMap(webhook -> webhook
                                        .execute().withContent(content)
                                        .withUsername(author.getDisplayName())
                                        .withAvatarUrl(author.getAvatarUrl())
                                        .withWaitForMessage(true)
                                ).block();

                                if (msg == null) return;

                                if (banlist.isBanned(player)) banlist.remove(player);

                                UserWhiteListEntry entry = new UserWhiteListEntry(player);
                                whitelist.add(entry);

                                state.pushWhitelist(author.getId(), msg.getId(), player.id());

                                msg.addReaction(Emoji.unicode("✅")).block();
                            }
                        }
                    })
            ).then();
            Mono<Void> reactEvent = gateway.on(ReactionAddEvent.class, event ->
                    Mono.fromRunnable(() -> {
                        Member member = event.getMember().orElse(null);
                        if (member == null) return;

                        if (!event.getChannelId().equals(CHANNEL) || member.isBot()) {
                            return;
                        }
                        Message message = event.getMessage().block();
                        if (message == null) return;

                        Emoji emoji = event.getEmoji();
                        WhitelistSate state = WhitelistSate.getWhitelistState(server);

                        if (member.getRoleIds().contains(ROLE)) {
                            if (emoji.equals(Emoji.unicode("✅"))) {
                                WhitelistSate.UserUuidPair pair = state.popWhitelistRequest(message.getId());
                                if (pair == null) {
                                    message.removeReaction(emoji, member.getId()).block();
                                    return;
                                }

                                PlayerList playerManager = server.getPlayerList();
                                UserWhiteList whitelist = playerManager.getWhiteList();
                                UserBanList banlist = playerManager.getBans();

                                NameAndId player = server.services().nameToIdCache().get(pair.uuid()).orElse(null);
                                if (player == null) {
                                    message.delete().block();
                                    return;
                                }

                                if (whitelist.isWhiteListed(player)) {
                                    message.delete().block();
                                    return;
                                }

                                if (banlist.isBanned(player)) banlist.remove(player);

                                UserWhiteListEntry entry = new UserWhiteListEntry(player);
                                whitelist.add(entry);

                                state.unban(pair.user());
                                state.pushWhitelist(pair.user(), message.getId(), player.id());

                                gateway.getWebhookById(WEBHOOK).flatMap(webhook -> webhook
                                        .editMessage(event.getMessageId(), WebhookMessageEditSpec.builder().content(Possible.of(Optional.of(player.name()))).build())
                                ).block();
                                message.removeAllReactions().block();
                                message.addReaction(Emoji.unicode("✅")).block();

                            } else if (emoji.equals(Emoji.unicode("❌"))) {
                                message.delete().block();

                                if (state.popWhitelistRequest(event.getMessageId()) != null) return;

                                WhitelistSate.UserUuidPair pair = state.popWhitelist(message.getId());

                                NameAndId player = server.services().nameToIdCache().get(pair.uuid()).orElse(null);
                                if (player == null) return;

                                PlayerList playerManager = server.getPlayerList();

                                UserWhiteList whitelist = playerManager.getWhiteList();
                                whitelist.remove(player);

                                ServerPlayer serverPlayerEntity = playerManager.getPlayer(player.id());
                                if (serverPlayerEntity == null) return;
                                serverPlayerEntity.connection.disconnect(Component.translatable("multiplayer.disconnect.not_whitelisted"));

                            } else if (emoji.equals(Emoji.unicode("⛔"))) {
                                message.delete().block();

                                if (state.popWhitelistRequest(event.getMessageId()) != null) return;

                                WhitelistSate.UserUuidPair pair = state.popWhitelist(message.getId());
                                state.ban(pair.user());

                                NameAndId player = server.services().nameToIdCache().get(pair.uuid()).orElse(null);

                                if (player == null) return;

                                PlayerList playerManager = server.getPlayerList();

                                UserBanList banList = playerManager.getBans();
                                UserBanListEntry bannedPlayerEntry = new UserBanListEntry(player);
                                banList.add(bannedPlayerEntry);

                                UserWhiteList whitelist = playerManager.getWhiteList();
                                whitelist.remove(player);

                                ServerPlayer serverPlayerEntity = playerManager.getPlayer(player.id());
                                if (serverPlayerEntity == null) return;
                                serverPlayerEntity.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));

                            } else {
                                message.removeReaction(emoji, member.getId()).block();
                            }
                        } else {
                            message.removeReaction(emoji, member.getId()).block();
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
