package com.mt1006.mocap.mixin.fields;

import net.minecraft.entity.BoostHelper;
import net.minecraft.entity.passive.PigEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PigEntity.class)
public interface PigMixin
{
	@Accessor BoostHelper getSteering();
}