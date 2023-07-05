package com.mt1006.mocap.events;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.mocap.actions.EntityUpdate;
import com.mt1006.mocap.mocap.actions.Hurt;
import com.mt1006.mocap.mocap.playing.Playing;
import com.mt1006.mocap.mocap.recording.Recording;
import com.mt1006.mocap.mocap.recording.TrackedEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MocapMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityEvent
{
	@SubscribeEvent
	public static void onEntityHurt(LivingAttackEvent attackEvent)
	{
		if (Recording.state == Recording.State.RECORDING && attackEvent.getEntity().level instanceof ServerLevel)
		{
			if (Recording.isRecordedPlayer(attackEvent.getEntity()))
			{
				new Hurt().write(Recording.writer);
			}
			else
			{
				TrackedEntity trackedEntity = Recording.getTrackedEntity(attackEvent.getEntity());
				if (trackedEntity != null) { new EntityUpdate(EntityUpdate.HURT, trackedEntity.id).write(Recording.writer); }
			}
		}
	}

	@SubscribeEvent
	public static void onEntityDrop(LivingDropsEvent dropsEvent)
	{
		if (Playing.playedScenes.size() > 0 && dropsEvent.getEntity().getTags().contains(Playing.MOCAP_ENTITY_TAG))
		{
			dropsEvent.setCanceled(true);
		}
	}
}
