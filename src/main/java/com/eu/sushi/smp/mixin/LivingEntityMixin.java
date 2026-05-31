package com.eu.sushi.smp.mixin;

import com.eu.sushi.smp.Smp;
import com.eu.sushi.smp.SpawnElytra;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "canGlide", at = @At(value = "HEAD"), cancellable = true)
    public void onCanGlide(CallbackInfoReturnable<Boolean> ci) {
        if (!Smp.config.spawnElytra.enabled) return;
        LivingEntity self = (LivingEntity) (Object) this;

        if (!self.onGround() && !self.isPassenger() && !self.hasEffect(MobEffects.LEVITATION)) {
            if (SpawnElytra.forceGlide(self)) {
                ci.setReturnValue(true);
            }
        } else {
            SpawnElytra.removePlayer(self);
        }
    }

    @Inject(method = "updateFallFlying", at = @At("HEAD"), cancellable = true)
    public void onTickGliding(CallbackInfo ci) {
        if (!Smp.config.spawnElytra.enabled) return;
        LivingEntity self = (LivingEntity) (Object) this;

        if (SpawnElytra.forceGlide(self)) {
            self.checkFallDistanceAccumulation();
            ci.cancel();
        }
    }
}
