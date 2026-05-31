package com.eu.sushi.smp.mixin;

import com.eu.sushi.smp.Smp;
import com.eu.sushi.smp.SpawnElytra;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.GameType;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        if (!Smp.config.spawnElytra.enabled) return;
        ServerPlayer self = (ServerPlayer) (Object) this;
        Abilities abilities = self.getAbilities();
        if (self.gameMode() != GameType.SURVIVAL && self.gameMode() != GameType.ADVENTURE) {
            return;
        }

        if (SpawnElytra.inSpawn(self)) {
            if (self.onGround()) {
                SpawnElytra.removePlayer(self);
            }

            if (SpawnElytra.forceGlide(self)) {
                abilities.mayfly = false;
            } else {
                if (abilities.flying) {
                    abilities.flying = false;
                    abilities.mayfly = false;
                    self.onUpdateAbilities();

                    SpawnElytra.addPlayer(self);
                    self.startFallFlying();

                    ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
                    rocket.set(DataComponents.FIREWORKS, new Fireworks(3, List.of()));
                    Projectile.spawnProjectile(new FireworkRocketEntity(self.level(), rocket, self), self.level(), rocket);
                } else {
                    abilities.mayfly = true;
                    self.onUpdateAbilities();
                }
            }
        } else {
            if (self.onGround()) {
                SpawnElytra.removePlayer(self);
            }
            abilities.mayfly = false;
            self.onUpdateAbilities();
        }
    }
}
