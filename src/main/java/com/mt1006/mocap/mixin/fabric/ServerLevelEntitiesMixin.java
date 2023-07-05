package com.mt1006.mocap.mixin.fabric;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

@Mixin(ServerLevel.class)
public interface ServerLevelEntitiesMixin
{
	@Accessor Map<UUID, Entity> getEntitiesByUuid();
}
