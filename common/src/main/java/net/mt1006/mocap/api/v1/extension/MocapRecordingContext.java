package net.mt1006.mocap.api.v1.extension;

import net.minecraft.world.entity.Entity;
import net.mt1006.mocap.api.v1.controller.playable.MocapActiveRecording;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

public interface MocapRecordingContext
{
	MocapActiveRecording asActiveRecording();

	void addAction(MocapAction action);

	void addEntityAction(MocapAction action, int entityId);

	Collection<? extends TrackedEntity> getTrackedEntities();

	TrackedEntity getTrackedEntity(Entity entity);

	State getState();

	int getTick();

	@ApiStatus.Internal
	MocapRecordingData getRecordingData();

	enum State
	{
		WAITING_FOR_ACTION(false),
		RECORDING(false),
		WAITING_FOR_DECISION(false),
		CANCELED(true),
		DISCARDED(true),
		SAVED(true),
		@ApiStatus.Internal UNDEFINED(true);

		public final boolean removed;

		State(boolean removed)
		{
			this.removed = removed;
		}
	}

	interface TrackedEntity
	{
		MocapRecordingContext getParent();

		int getId();

		Entity getEntity();
	}
}
