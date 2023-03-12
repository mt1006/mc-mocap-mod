package com.mt1006.mocap.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.datasync.DataParameter;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityMixin
{
	@Accessor static @Nullable DataParameter<Byte> getDATA_LIVING_ENTITY_FLAGS() { return null; }
}
