package net.mt1006.mocap.fabric.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.mt1006.mocap.events.EntityEvent;

public class EntityFabricEvent
{
	public static boolean onEntityHurt(LivingEntity entity, DamageSource source, float amount)
	{
		EntityEvent.onEntityHurt(entity);
		return true;
	}

	public static void onPlayerRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean keepInventory)
	{
		EntityEvent.onPlayerRespawn(oldPlayer, newPlayer);
	}
}
