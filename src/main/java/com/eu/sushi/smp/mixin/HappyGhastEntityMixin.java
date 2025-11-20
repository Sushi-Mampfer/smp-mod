package com.eu.sushi.smp.mixin;

import com.eu.sushi.smp.SmpEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static com.eu.sushi.smp.Smp.LOGGER;
import static com.eu.sushi.smp.Smp.MOD_ID;

@Mixin(HappyGhastEntity.class)
public class HappyGhastEntityMixin {
    @Inject(method = "removePassenger", at = @At("TAIL"))
    private void onRemovePassenger(CallbackInfo ci) {
        HappyGhastEntity self = (HappyGhastEntity) (Object) this;

        if (!self.hasPassengers()) {
            EntityAttributeInstance instance = self.getAttributeInstance(EntityAttributes.FLYING_SPEED);
            if (instance == null) return;

            Identifier id = Identifier.of(MOD_ID, "speedy_ghast");
            instance.removeModifier(id);
        }
    }

    @Inject(method = "getControlledMovementInput", at = @At("HEAD"))
    private void onGetControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput, CallbackInfoReturnable<Vec3d> cir) {
        HappyGhastEntity self = (HappyGhastEntity) (Object) this;

        EntityAttributeInstance instance = self.getAttributeInstance(EntityAttributes.FLYING_SPEED);
        if (instance == null) return;

        Identifier id = Identifier.of(MOD_ID, "speedy_ghast");

        boolean hasModifier = instance.hasModifier(id);

        if (!(controllingPlayer instanceof ServerPlayerEntity pilot)) {
            if (hasModifier) {
                instance.removeModifier(id);
            }
            return;
        }


        RegistryEntryLookup<Enchantment> lookup = self.getEntityWorld().getRegistryManager().getOptional(RegistryKeys.ENCHANTMENT).get();
        Optional<RegistryEntry.Reference<Enchantment>> enchantment = lookup.getOptional(SmpEnchantments.SPEEDY_GHAST);

        if (enchantment.isEmpty()) {
            if (hasModifier) {
                instance.removeModifier(id);
            }
        }

        int level = self.getBodyArmor().getEnchantments().getLevel(enchantment.orElse(null));

        if (pilot.getPlayerInput().sprint()) {
            if (!hasModifier) {
                EntityAttributeModifier modifier = new EntityAttributeModifier(id, level / 2.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                instance.addTemporaryModifier(modifier);
            }
        } else {
            if (hasModifier) {
                instance.removeModifier(id);
            }
        }
    }
}
