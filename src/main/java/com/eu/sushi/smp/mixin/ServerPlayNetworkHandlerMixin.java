package com.eu.sushi.smp.mixin;

import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Redirect(method = "onVehicleMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;isHost()Z"))
    private boolean isHost(ServerPlayNetworkHandler instance) {
        return instance.player.getRootVehicle() instanceof HappyGhastEntity;
    }
}
