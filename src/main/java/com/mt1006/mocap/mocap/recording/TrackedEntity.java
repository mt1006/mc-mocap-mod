package com.mt1006.mocap.mocap.recording;

import com.mt1006.mocap.mocap.actions.EntityUpdate;
import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.EntityState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class TrackedEntity
{
	public final int id;
	private @Nullable EntityState previousState = null;
	public boolean dying;
	public int lastTick;

	public TrackedEntity(int id)
	{
		this.id = id;
	}

	public void onTick(RecordingFiles.Writer recording, Entity entity, int tick)
	{
		EntityState state = new EntityState(entity);

		state.saveTrackedEntityDifference(recording, id, previousState);
		previousState = state;
		lastTick = tick;

		if (entity instanceof LivingEntity && ((LivingEntity)entity).isDeadOrDying() && !dying)
		{
			new EntityUpdate(EntityUpdate.KILL, id).write(Recording.writer);
			dying = true;
		}
	}
}
