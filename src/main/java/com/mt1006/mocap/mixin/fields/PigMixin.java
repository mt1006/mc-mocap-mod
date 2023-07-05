package com.mt1006.mocap.mixin.fields;

import net.minecraft.entity.passive.PigEntity;
import net.minecraft.network.datasync.DataParameter;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PigEntity.class)
public interface PigMixin
{
	@Accessor static @Nullable DataParameter<Boolean> getDATA_SADDLE_ID() { return null; }
}