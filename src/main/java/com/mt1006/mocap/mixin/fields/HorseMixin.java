package com.mt1006.mocap.mixin.fields;

import net.minecraft.entity.passive.horse.HorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HorseEntity.class)
public interface HorseMixin
{
	@Invoker void callSetTypeVariant(int val);
	@Invoker int callGetTypeVariant();
}
