package net.mt1006.mocap.mocap.actions.deprecated;

import net.minecraft.world.entity.Pose;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;

public class ChangePose implements MocapAction
{
	private final Pose pose;

	public ChangePose(Reader reader)
	{
		pose = Pose.BY_ID.apply(reader.readInt() - 1);
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		//TODO: [CONVERTER] remove
		writer.addInt(pose.id() + 1);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		ctx.getEntity().setPose(pose);
		return Result.OK;
	}
}
