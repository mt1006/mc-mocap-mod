package com.mt1006.mocap.mocap.recording;

import com.mt1006.mocap.mocap.actions.ComparableAction;
import com.mt1006.mocap.mocap.actions.EntityAction;
import com.mt1006.mocap.mocap.actions.Movement;
import com.mt1006.mocap.mocap.actions.NextTick;
import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.RecordingData;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EntityState
{
	public final List<ComparableAction> actions;

	public EntityState(Entity entity)
	{
		actions = new ArrayList<>(ComparableAction.REGISTERED.size());
		ComparableAction.REGISTERED.forEach((constructor) -> actions.add(constructor.apply(entity)));
	}

	public boolean differs(@Nullable EntityState previousActions)
	{
		if (previousActions == null) { return false; }
		for (int i = 0; i < ComparableAction.REGISTERED.size(); i++)
		{
			if (actions.get(i).differs(previousActions.actions.get(i))) { return true; }
		}
		return false;
	}

	public void saveDifference(RecordingFiles.Writer writer, @Nullable EntityState previousActions)
	{
		if (previousActions != null)
		{
			for (int i = 0; i < ComparableAction.REGISTERED.size(); i++)
			{
				actions.get(i).write(writer, previousActions.actions.get(i));
			}
		}
		else
		{
			writer.addByte(RecordingFiles.RECORDING_VERSION);
			find(Movement.class).writeAsHeader(writer);
			Recording.endsWithDeathPos = writer.addMutableBoolean(false);

			actions.forEach((action) -> action.write(writer, null));
		}
		new NextTick().write(writer);
	}

	public void saveTrackedEntityDifference(RecordingFiles.Writer writer, int id, @Nullable EntityState previousActions)
	{
		if (previousActions != null)
		{
			for (int i = 0; i < ComparableAction.REGISTERED.size(); i++)
			{
				new EntityAction(id, actions.get(i)).write(writer, previousActions.actions.get(i));
			}
		}
		else
		{
			actions.forEach((action) -> new EntityAction(id, action).write(writer, null));
		}
	}

	public static void initEntity(Entity entity, RecordingData recordingData, Vector3d posOffset)
	{
		entity.moveTo(
				recordingData.startPos[0] + posOffset.x,
				recordingData.startPos[1] + posOffset.y,
				recordingData.startPos[2] + posOffset.z,
				recordingData.startRot[0],
				recordingData.startRot[1]);
	}

	private <T extends ComparableAction> T find(Class<T> type)
	{
		for (ComparableAction action : actions)
		{
			if (type.isAssignableFrom(action.getClass())) { return (T)action; }
		}
		throw new RuntimeException("Failed to find action with given type!");
	}
}
