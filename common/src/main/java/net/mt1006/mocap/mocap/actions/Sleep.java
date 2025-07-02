package net.mt1006.mocap.mocap.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapStateAction;
import org.jetbrains.annotations.Nullable;

public class Sleep implements MocapStateAction
{
	private final @Nullable BlockPos bedPostion;

	public Sleep(Entity entity)
	{
		bedPostion = (entity instanceof LivingEntity) ? ((LivingEntity)entity).getSleepingPos().orElse(null) : null;
	}

	public Sleep(Reader reader)
	{
		bedPostion = reader.readBoolean() ? reader.readBlockPos() : null;
	}

	@Override public boolean differs(MocapStateAction previousAction)
	{
		if (bedPostion == null && ((Sleep)previousAction).bedPostion == null) { return false; }
		if ((bedPostion == null) != (((Sleep)previousAction).bedPostion == null)) { return true; }
		return bedPostion != null && !bedPostion.equals(((Sleep)previousAction).bedPostion);
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
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
