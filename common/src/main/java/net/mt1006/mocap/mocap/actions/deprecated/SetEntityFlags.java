package net.mt1006.mocap.mocap.actions.deprecated;

import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.utils.EntityData;

public class SetEntityFlags implements MocapAction
{
	private final byte flags;

	public SetEntityFlags(Reader reader)
	{
		flags = reader.readByte();
	}

	public void write(Writer writer, MocapRecordingData data)
	{
		//TODO: [CONVERTER] remove
		writer.addByte(flags);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		EntityData.ENTITY_FLAGS.set(ctx.getEntity(), flags);
		return Result.OK;
	}
}
