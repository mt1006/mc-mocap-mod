package net.mt1006.mocap.mocap.actions;

import net.minecraft.world.entity.Entity;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapStateAction;
import net.mt1006.mocap.utils.EntityData;

public class SetEntityFlags implements MocapStateAction
{
	private final byte flags;

	public SetEntityFlags(Entity entity)
	{
		//TODO: create tests in case flag order changes
		//TODO: test fire
		this.flags = EntityData.ENTITY_FLAGS.valOrDef(entity, (byte)0);
	}

	public SetEntityFlags(Reader reader)
	{
		flags = reader.readByte();
	}

	@Override public boolean differs(MocapStateAction previousAction)
	{
		return flags != ((SetEntityFlags)previousAction).flags;
	}

	public void write(Writer writer, MocapRecordingData data)
	{
		writer.addByte(flags);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		EntityData.ENTITY_FLAGS.set(ctx.getEntity(), flags);
		return Result.OK;
	}
}
