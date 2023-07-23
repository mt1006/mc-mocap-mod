package com.mt1006.mocap.mixin.fields;

import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.animal.Pig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Pig.class)
public interface PigMixin
{
	@Accessor ItemBasedSteering getSteering();
}