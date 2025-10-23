package net.mt1006.mocap.neoforge.events;

import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.events.EntityEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = MocapMod.MOD_ID)
public class EntityNeoForgeEvent
{
	@SubscribeEvent
	public static void onEntityHurt(LivingDamageEvent.Post damageEvent)
	{
		if (damageEvent.getNewDamage() != 0.0f || damageEvent.getBlockedDamage() == 0.0f)
		{
			EntityEvent.onEntityHurt(damageEvent.getEntity());
		}
	}

	@SubscribeEvent
	public static void onEntityDrop(LivingDropsEvent dropsEvent)
	{
		if (EntityEvent.onEntityDrop(dropsEvent.getEntity())) { dropsEvent.setCanceled(true); }
	}
}
