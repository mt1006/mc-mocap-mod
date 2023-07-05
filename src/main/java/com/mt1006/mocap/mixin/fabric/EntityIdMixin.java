package com.mt1006.mocap.mixin.fabric;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityIdMixin
{
	@Invoker @Nullable String callGetEncodeId();
}
