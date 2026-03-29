package net.mt1006.mocap.mocap.recording;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.mt1006.mocap.api.v1.extension.MocapRecordingContext;
import net.mt1006.mocap.api.v1.modifiers.MocapEntityFilter;
import net.mt1006.mocap.mixin.fields.LevelFields;
import net.mt1006.mocap.mocap.actions.EntityUpdate;
import net.mt1006.mocap.mocap.playing.PlaybackManager;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EntityTracker
{
	private final RecordingContext ctx;
	private final Map<Entity, TrackedEntity> map = new HashMap<>();
	private int counter = 0;
	private Entity playerVehicle = null;

	public EntityTracker(RecordingContext ctx)
	{
		this.ctx = ctx;
	}
	
	public TrackedEntity get(Entity entity)
	{
		return map.get(entity);
	}

	public Collection<TrackedEntity> getAll()
	{
		return map.values();
	}

	public void onTick()
	{
		if (ctx.config.getEntityTrackingDistance() != 0.0)
		{
			updateTracked();
			updateVehicle();
		}
		removeOld();
	}

	private void updateTracked()
	{
		double entityTrackingDist = ctx.config.getEntityTrackingDistance();
		boolean limitDistance = entityTrackingDist >= 0.0;
		double maxDistanceSqr = entityTrackingDist * entityTrackingDist;
		MocapEntityFilter filter = ctx.config.getTrackEntities();

		for (Entity entity : ((LevelFields)ctx.recordedPlayer.level()).callGetEntities().getAll())
		{
			if ((limitDistance && ctx.recordedPlayer.distanceToSqr(entity) > maxDistanceSqr) || entity instanceof Player
					|| (ctx.config.getPreventTrackingPlayedEntities() && entity.entityTags().contains(PlaybackManager.MOCAP_ENTITY_TAG)))
			{
				continue;
			}

			if (!filter.isAllowed(entity)) { continue; }

			TrackedEntity trackedEntity = map.get(entity);
			if (trackedEntity == null)
			{
				trackedEntity = new TrackedEntity(ctx, counter++, entity);
				map.put(entity, trackedEntity);
				ctx.addAction(EntityUpdate.addEntity(trackedEntity.id, entity, ctx.config));
			}
			trackedEntity.onTick();
		}
	}

	private void updateVehicle()
	{
		Entity newPlayerVehicle = ctx.recordedPlayer.getVehicle();
		if (newPlayerVehicle == null)
		{
			if (playerVehicle != null)
			{
				ctx.addAction(EntityUpdate.playerDismount());
				playerVehicle = null;
			}
			return;
		}

		if (newPlayerVehicle.equals(playerVehicle)) { return; }

		if (playerVehicle != null)
		{
			ctx.addAction(EntityUpdate.playerDismount());
			playerVehicle = null;
		}

		TrackedEntity trackedEntity = map.get(newPlayerVehicle);
		if (trackedEntity != null)
		{
			ctx.addAction(EntityUpdate.playerMount(trackedEntity.id));
			playerVehicle = newPlayerVehicle;
		}
	}

	private void removeOld()
	{
		int tick = ctx.getTick();
		List<Entity> toRemove = new ArrayList<>();

		for (Map.Entry<Entity, TrackedEntity> entry : map.entrySet())
		{
			if (entry.getValue().lastTick == tick) { continue; }

			if (!entry.getValue().dying)
			{
				int entityId = entry.getValue().id;
				EntityUpdate entityUpdate = entry.getKey().getRemovalReason() == Entity.RemovalReason.KILLED
						? EntityUpdate.kill(entityId) : EntityUpdate.removeEntity(entityId);
				ctx.addAction(entityUpdate);
			}
			toRemove.add(entry.getKey());
		}
		toRemove.forEach(map::remove);
	}

	public static class TrackedEntity implements MocapRecordingContext.TrackedEntity
	{
		private final RecordingContext ctx;
		private final int id;
		private final Entity entity;
		private final PositionTracker positionTracker;
		private @Nullable RecordedEntityState previousState = null;
		private boolean dying;
		private int lastTick;

		public TrackedEntity(RecordingContext ctx, int id, Entity entity)
		{
			this.ctx = ctx;
			this.id = id;
			this.entity = entity;
			this.positionTracker = new PositionTracker(entity, true, ctx.data.startPos);
		}

		public void onTick()
		{
			RecordedEntityState state = new RecordedEntityState(entity);
			state.saveTrackedEntityDifference(ctx.data.actions, id, previousState);
			previousState = state;

			positionTracker.onTick(ctx.data.actions, id);
			lastTick = ctx.getTick();

			if (entity instanceof LivingEntity && ((LivingEntity)entity).isDeadOrDying() && !dying)
			{
				ctx.addAction(EntityUpdate.kill(id));
				dying = true;
			}
		}

		@Override public MocapRecordingContext getParent()
		{
			return ctx;
		}

		@Override public int getId()
		{
			return id;
		}

		@Override public Entity getEntity()
		{
			return entity;
		}
	}
}
