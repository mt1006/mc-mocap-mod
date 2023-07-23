package com.mt1006.mocap.events;

import com.mt1006.mocap.mocap.actions.EntityUpdate;
import com.mt1006.mocap.mocap.actions.Hurt;
import com.mt1006.mocap.mocap.playing.Playing;
import com.mt1006.mocap.mocap.recording.Recording;
import com.mt1006.mocap.mocap.recording.TrackedEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class EntityEvent
{
	public static boolean onEntityHurt(LivingEntity entity, DamageSource source, float amount)
	{
		if (Recording.state == Recording.State.RECORDING && entity.level instanceof ServerLevel)
		{
			if (Recording.isRecordedPlayer(entity))
			{
				Hurt.write(Recording.writer);
			}
			else
			{
				TrackedEntity trackedEntity = Recording.getTrackedEntity(entity);
				if (trackedEntity != null) { new EntityUpdate(EntityUpdate.HURT, trackedEntity.id).write(Recording.writer); }
			}
		}
		return true;
	}

	public static boolean onEntityDrop(LivingEntity entity)
	{
		return Playing.playedScenes.size() > 0 && entity.getTags().contains(Playing.MOCAP_ENTITY_TAG);
	}
}
