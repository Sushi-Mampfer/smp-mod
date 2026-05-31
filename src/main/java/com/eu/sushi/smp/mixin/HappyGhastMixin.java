package com.eu.sushi.smp.mixin;

import com.eu.sushi.smp.enchantments.SmpEnchantments;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;

import static com.eu.sushi.smp.Smp.MOD_ID;

@Mixin(HappyGhast.class)
public class HappyGhastMixin {
    @Inject(method = "removePassenger", at = @At("TAIL"))
    private void onRemovePassenger(CallbackInfo ci) {
        HappyGhast self = (HappyGhast) (Object) this;

        if (!self.isVehicle()) {
            AttributeInstance instance = self.getAttribute(Attributes.FLYING_SPEED);
            if (instance == null) return;

            Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, "speedy_ghast");
            instance.removeModifier(id);
        }
    }

    @Inject(method = "getRiddenInput", at = @At("HEAD"))
    private void onGetControlledMovementInput(Player controller, Vec3 selfInput, CallbackInfoReturnable<Vec3> cir) {
        HappyGhast self = (HappyGhast) (Object) this;

        AttributeInstance instance = self.getAttribute(Attributes.FLYING_SPEED);
        if (instance == null) return;

        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, "speedy_ghast");

        boolean hasModifier = instance.hasModifier(id);

        if (!(controller instanceof ServerPlayer pilot)) {
            if (hasModifier) {
                instance.removeModifier(id);
            }
            return;
        }


        HolderGetter<Enchantment> lookup = self.level().registryAccess().lookup(Registries.ENCHANTMENT).get();
        Optional<Holder.Reference<Enchantment>> enchantment = lookup.get(SmpEnchantments.SPEEDY_GHAST);

        if (enchantment.isEmpty()) {
            if (hasModifier) {
                instance.removeModifier(id);
            }
            return;
        }

        int level = self.getBodyArmorItem().getEnchantments().getLevel(enchantment.orElse(null));

        if (pilot.getLastClientInput().sprint()) {
            if (!hasModifier) {
                AttributeModifier modifier = new AttributeModifier(id, level / 2.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                instance.addTransientModifier(modifier);
            }
        } else {
            if (hasModifier) {
                instance.removeModifier(id);
            }
        }
    }
}
