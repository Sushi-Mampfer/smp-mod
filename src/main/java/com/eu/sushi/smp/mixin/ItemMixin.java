package com.eu.sushi.smp.mixin;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Item.class)
public class ItemMixin {
    @Redirect(method = "<clinit>", at = @At(value = "NEW", target = "net/minecraft/item/Lig"))
    private static SnowballItem modifySnowball(Item.Settings settings) {
        return new SnowballItem(new Item.Settings().maxCount(32)); // modify stack size to 32
    }
}
