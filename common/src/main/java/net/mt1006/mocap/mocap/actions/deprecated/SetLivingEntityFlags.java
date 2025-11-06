package net.mt1006.mocap.mocap.actions.deprecated;

import net.minecraft.world.entity.LivingEntity;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.utils.EntityData;

public class SetLivingEntityFlags implements MocapAction
{
	private final byte flags;

	public SetLivingEntityFlags(Reader reader)
	{
		flags = reader.readByte();
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		//TODO: [CONVERTER] remove
		writer.addByte(flags);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (!(ctx.getEntity() instanceof LivingEntity)) { return Result.IGNORED; }
		EntityData.LIVING_ENTITY_FLAGS.set(ctx.getEntity(), flags);
		return Result.OK;
	}
}
