package com.mt1006.mocap.events;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.mocap.actions.EntityUpdate;
import com.mt1006.mocap.mocap.actions.Hurt;
import com.mt1006.mocap.mocap.playing.Playing;
import com.mt1006.mocap.mocap.recording.Recording;
import com.mt1006.mocap.mocap.recording.TrackedEntity;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = MocapMod.MOD_ID)
public class EntityEvent
{
	@SubscribeEvent
	public static void onEntityHurt(LivingDamageEvent.Post damageEvent)
	{
		if (Recording.state == Recording.State.RECORDING && damageEvent.getEntity().level() instanceof ServerLevel)
		{
			if (Recording.isRecordedPlayer(damageEvent.getEntity()))
			{
				Hurt.write(Recording.writer);
			}
			else
			{
				TrackedEntity trackedEntity = Recording.getTrackedEntity(damageEvent.getEntity());
				if (trackedEntity != null) { new EntityUpdate(EntityUpdate.HURT, trackedEntity.id).write(Recording.writer); }
			}
		}
	}

	@SubscribeEvent
	public static void onEntityDrop(LivingDropsEvent dropsEvent)
	{
		if (Playing.playedScenes.size() > 0 && dropsEvent.getEntity().entityTags().contains(Playing.MOCAP_ENTITY_TAG))
		{
			dropsEvent.setCanceled(true);
		}
	}
}
