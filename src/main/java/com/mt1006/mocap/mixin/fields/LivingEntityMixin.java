package com.mt1006.mocap.mixin.fields;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(LivingEntity.class)
public interface LivingEntityMixin
{
	@Accessor static @Nullable DataParameter<Byte> getDATA_LIVING_ENTITY_FLAGS() { return null; }
	@Accessor static @Nullable DataParameter<Integer> getDATA_EFFECT_COLOR_ID() { return null; }
	@Accessor static @Nullable DataParameter<Boolean> getDATA_EFFECT_AMBIENCE_ID() { return null; }
	@Accessor static @Nullable DataParameter<Integer> getDATA_ARROW_COUNT_ID() { return null; }
	@Accessor static @Nullable DataParameter<Integer> getDATA_STINGER_COUNT_ID() { return null; }
	@Accessor static @Nullable DataParameter<Optional<BlockPos>> getSLEEPING_POS_ID() { return null; }
}