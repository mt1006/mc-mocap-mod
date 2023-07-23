package com.mt1006.mocap.mixin.fields;

import net.minecraft.entity.Entity;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

@Mixin(ServerWorld.class)
public interface LevelMixin
{
	@Accessor Map<UUID, Entity> getEntitiesByUuid();
}
