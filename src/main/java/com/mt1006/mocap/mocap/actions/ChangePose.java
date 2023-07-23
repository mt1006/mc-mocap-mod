package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Pose;
import org.jetbrains.annotations.Nullable;

public class ChangePose implements ComparableAction
{
	private final Pose pose;

	public ChangePose(Entity entity)
	{
		pose = entity.getPose();
	}

	public ChangePose(RecordingFiles.Reader reader)
	{
		switch (reader.readInt())
		{
			default: pose = Pose.STANDING; break;
			case 2: pose = Pose.FALL_FLYING; break;
			case 3: pose = Pose.SLEEPING; break;
			case 4: pose = Pose.SWIMMING; break;
			case 5: pose = Pose.SPIN_ATTACK; break;
			case 6: pose = Pose.CROUCHING; break;
			case 7: pose = Pose.DYING; break;
		}
	}

	@Override public boolean differs(ComparableAction action)
	{
		return pose != ((ChangePose)action).pose;
	}

	@Override public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action)
	{
		if (action != null && !differs(action)) { return; }
		writer.addByte(Type.CHANGE_POSE.id);

		switch (pose)
		{
			case STANDING: writer.addInt(1); break;
			case FALL_FLYING: writer.addInt(2); break;
			case SLEEPING: writer.addInt(3); break;
			case SWIMMING: writer.addInt(4); break;
			case SPIN_ATTACK: writer.addInt(5); break;
			case CROUCHING: writer.addInt(6); break;
			case DYING: writer.addInt(7); break;
			default: writer.addInt(0);
		}
	}

	@Override public Result execute(PlayingContext ctx)
	{
		ctx.entity.setPose(pose);
		return Result.OK;
	}
}
