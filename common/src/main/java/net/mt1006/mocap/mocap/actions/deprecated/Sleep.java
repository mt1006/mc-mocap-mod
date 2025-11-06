package net.mt1006.mocap.mocap.actions.deprecated;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import org.jetbrains.annotations.Nullable;

public class Sleep implements MocapAction
{
	private final @Nullable BlockPos bedPostion;

	public Sleep(Reader reader)
	{
		bedPostion = reader.readBoolean() ? reader.readBlockPos() : null;
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		//TODO: [CONVERTER] remove
		if (bedPostion != null)
		{
			writer.addBoolean(true);
			writer.addBlockPos(bedPostion);
		}
		else
		{
			writer.addBoolean(false);
		}
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (!(ctx.getEntity() instanceof LivingEntity)) { return Result.IGNORED; }

		if (bedPostion != null) { ((LivingEntity)ctx.getEntity()).setSleepingPos(bedPostion); }
		else { ((LivingEntity)ctx.getEntity()).clearSleepingPos(); }
		return Result.OK;
	}
}
