package com.mt1006.mocap.mixin.fields;

import net.minecraft.world.entity.animal.horse.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Horse.class)
public interface HorseMixin
{
	@Invoker void callSetTypeVariant(int val);
	@Invoker int callGetTypeVariant();
}
