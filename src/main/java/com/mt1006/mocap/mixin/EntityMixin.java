package com.mt1006.mocap.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Pose;
import net.minecraft.network.datasync.DataParameter;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityMixin
{
	@Accessor static @Nullable DataParameter<Byte> getDATA_SHARED_FLAGS_ID() { return null; }
	@Accessor static @Nullable DataParameter<Pose> getDATA_POSE() { return null; }
}
