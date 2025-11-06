package net.mt1006.mocap.mocap.actions.deprecated;

import net.minecraft.world.entity.LivingEntity;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;

public class SetArrowCount implements MocapAction
{
	private final int arrowCount;
	private final int beeStingerCount;

	public SetArrowCount(Reader reader)
	{
		arrowCount = reader.readInt();
		beeStingerCount = reader.readInt();
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		//TODO: [CONVERTER] remove
		writer.addInt(arrowCount);
		writer.addInt(beeStingerCount);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (!(ctx.getEntity() instanceof LivingEntity entity)) { return Result.IGNORED; }
		entity.setArrowCount(arrowCount);
		entity.setStingerCount(beeStingerCount);
		return Result.OK;
	}
}
