package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
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
			case 8: pose = Pose.LONG_JUMPING; break;
			case 9: pose = Pose.CROAKING; break;
			case 10: pose = Pose.USING_TONGUE; break;
			case 12: pose = Pose.ROARING; break;
			case 13: pose = Pose.SNIFFING; break;
			case 14: pose = Pose.EMERGING; break;
			case 15: pose = Pose.DIGGING; break;
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
			case LONG_JUMPING: writer.addInt(8); break;
			case CROAKING: writer.addInt(9); break;
			case USING_TONGUE: writer.addInt(10); break;
			case ROARING: writer.addInt(12); break;
			case SNIFFING: writer.addInt(13); break;
			case EMERGING: writer.addInt(14); break;
			case DIGGING: writer.addInt(15); break;
			default: writer.addInt(0);
		}
	}

	@Override public Result execute(PlayingContext ctx)
	{
		ctx.entity.setPose(pose);
		return Result.OK;
	}
}
