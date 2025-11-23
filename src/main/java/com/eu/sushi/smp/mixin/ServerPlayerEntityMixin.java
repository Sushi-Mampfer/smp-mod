package com.eu.sushi.smp.mixin;

import com.eu.sushi.smp.SpawnElytra;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        PlayerAbilities abilities = self.getAbilities();
        if (self.getGameMode() != GameMode.SURVIVAL && self.getGameMode() != GameMode.ADVENTURE) {
            return;
        }

        if (SpawnElytra.inSpawn(self)) {
            if (self.isOnGround()) {
                SpawnElytra.removePlayer(self);
            }

            if (SpawnElytra.forceGlide(self)) {
                abilities.allowFlying = false;
            } else {
                if (abilities.flying) {
                    abilities.flying = false;
                    abilities.allowFlying = false;
                    self.sendAbilitiesUpdate();

                    SpawnElytra.addPlayer(self);
                    self.startGliding();

                    ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
                    rocket.set(DataComponentTypes.FIREWORKS, new FireworksComponent(3, List.of()));
                    ProjectileEntity.spawn(new FireworkRocketEntity(self.getEntityWorld(), rocket, self), self.getEntityWorld(), rocket);
                } else {
                    abilities.allowFlying = true;
                    self.sendAbilitiesUpdate();
                }
            }
        } else {
            if (self.isOnGround()) {
                SpawnElytra.removePlayer(self);
            }
            abilities.allowFlying = false;
            self.sendAbilitiesUpdate();
        }
    }
}
