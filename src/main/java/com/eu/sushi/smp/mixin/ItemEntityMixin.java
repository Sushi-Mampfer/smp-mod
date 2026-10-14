package com.eu.sushi.smp.mixin;

import com.eu.sushi.smp.Smp;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow
    private int age;

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V", at = @At("RETURN"))
    public void setAge(Level level, double x, double y, double z, ItemStack itemStack, CallbackInfo ci) {
        if (!level.isClientSide()) {
            Integer despawnTime = Smp.config.despawnBlocks.get(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getPath());
            if (despawnTime != null) {
                this.age = 6000 - despawnTime;
            }
        }
    }
}
