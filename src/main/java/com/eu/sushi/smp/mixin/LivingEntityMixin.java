package com.eu.sushi.smp.mixin;

import com.eu.sushi.smp.SpawnElytra;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "canGlide", at = @At(value = "HEAD"), cancellable = true)
    public void onCanGlide(CallbackInfoReturnable<Boolean> ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (!self.isOnGround() && !self.hasVehicle() && !self.hasStatusEffect(StatusEffects.LEVITATION)) {
            if (SpawnElytra.forceGlide(self)) {
                ci.setReturnValue(true);
            }
        } else {
            SpawnElytra.removePlayer(self);
        }
    }

    @Inject(method = "tickGliding", at = @At("HEAD"), cancellable = true)
    public void onTickGliding(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (SpawnElytra.forceGlide(self)) {
            self.limitFallDistance();
            ci.cancel();
        }
    }
}
