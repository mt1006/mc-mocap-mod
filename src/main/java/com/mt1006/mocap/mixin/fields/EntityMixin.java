package com.mt1006.mocap.mixin.fields;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityMixin
{
	@Accessor static @Nullable EntityDataAccessor<Byte> getDATA_SHARED_FLAGS_ID() { return null; }
	@Invoker void callCheckInsideBlocks();
}
