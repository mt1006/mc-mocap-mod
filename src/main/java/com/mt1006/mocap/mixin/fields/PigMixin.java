package com.mt1006.mocap.mixin.fields;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.Pig;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Pig.class)
public interface PigMixin
{
	@Accessor static @Nullable EntityDataAccessor<Boolean> getDATA_SADDLE_ID() { return null; }
}