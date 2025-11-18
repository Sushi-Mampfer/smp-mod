package com.eu.sushi.smp.mixin;

import com.eu.sushi.smp.Smp;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HappyGhastEntity.class)
public class HappyGhastEntityMixin {
    @Inject(method = "travel", at = @At("HEAD"))
    private void onTravel(CallbackInfo ci) {
        HappyGhastEntity self = (HappyGhastEntity) (Object) this;

        EntityAttributeInstance instance = self.getAttributeInstance(EntityAttributes.FLYING_SPEED);
        if (instance == null) return;

        Identifier id = Identifier.of("smp", "speedy_ghast");

        boolean hasModifier = instance.hasModifier(id);

        LivingEntity pilot = self.getControllingPassenger();
        if (pilot == null) {
            if (hasModifier) {
                instance.removeModifier(id);
            }
            return;
        };

        Smp.LOGGER.info("player");

        ServerPlayerEntity player = (ServerPlayerEntity) pilot;
        if (player.getPlayerInput().sprint()) {
            if (!hasModifier) {
                instance.addTemporaryModifier(new EntityAttributeModifier(id, 1.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        } else {
            if (hasModifier) {
                instance.removeModifier(id);
            }
        }
    }
}
