package com.mt1006.mocap.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityMixin
{
	@Accessor static @Nullable EntityDataAccessor<Byte> getDATA_SHARED_FLAGS_ID() { return null; }
	@Accessor static @Nullable EntityDataAccessor<Pose> getDATA_POSE() { return null; }
}
