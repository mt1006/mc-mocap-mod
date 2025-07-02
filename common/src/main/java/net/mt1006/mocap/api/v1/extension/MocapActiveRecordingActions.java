package net.mt1006.mocap.api.v1.extension;

import net.minecraft.world.entity.Entity;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;

import java.util.Collection;

public interface MocapActiveRecordingActions
{
	void addAction(MocapAction action);

	void addEntityAction(MocapAction action, int entityId);

	Collection<? extends TrackedEntity> getTrackedEntities();

	interface TrackedEntity
	{
		MocapActiveRecordingActions getParent();

		int getId();

		Entity getEntity();
	}
}
