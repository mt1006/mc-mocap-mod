package com.mt1006.mocap.mixin.fields;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(LivingEntity.class)
public interface LivingEntityMixin
{
	@Accessor static @Nullable EntityDataAccessor<Byte> getDATA_LIVING_ENTITY_FLAGS() { return null; }
	@Accessor static @Nullable EntityDataAccessor<Integer> getDATA_EFFECT_COLOR_ID() { return null; }
	@Accessor static @Nullable EntityDataAccessor<Boolean> getDATA_EFFECT_AMBIENCE_ID() { return null; }
	@Accessor static @Nullable EntityDataAccessor<Integer> getDATA_ARROW_COUNT_ID() { return null; }
	@Accessor static @Nullable EntityDataAccessor<Integer> getDATA_STINGER_COUNT_ID() { return null; }
	@Accessor static @Nullable EntityDataAccessor<Optional<BlockPos>> getSLEEPING_POS_ID() { return null; }
}