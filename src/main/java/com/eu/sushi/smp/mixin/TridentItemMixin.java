package com.eu.sushi.smp.mixin;

import com.eu.sushi.smp.SpawnElytra;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.eu.sushi.smp.Smp.LOGGER;

@Mixin(TridentItem.class)
public class TridentItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void onUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
        LOGGER.info("Called Use");
        ItemStack itemStack = user.getStackInHand(hand);
        if (EnchantmentHelper.getTridentSpinAttackStrength(itemStack, user) > 0.0F && SpawnElytra.forceGlide(user)) {
            LOGGER.info("Canceled use");
            ci.setReturnValue(ActionResult.FAIL);
        }
    }
    @Inject(method = "onStoppedUsing", at = @At("HEAD"), cancellable = true)
    public void onOnStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfoReturnable<Boolean> ci) {
        LOGGER.info("Called onUse");
        if (EnchantmentHelper.getTridentSpinAttackStrength(stack, user) > 0.0F && SpawnElytra.forceGlide(user)) {
            LOGGER.info("Canceled onUse");
            ci.setReturnValue(false);
        }
    }
}
