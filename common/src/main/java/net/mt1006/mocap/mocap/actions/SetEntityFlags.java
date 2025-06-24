package net.mt1006.mocap.mocap.actions;

import net.minecraft.world.entity.Entity;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapStateAction;
import net.mt1006.mocap.utils.EntityData;

public class SetEntityFlags implements MocapStateAction
{
	private final byte entityFlags;

	public SetEntityFlags(Entity entity)
	{
		//TODO: test fire
		this.entityFlags = EntityData.ENTITY_FLAGS.valOrDef(entity, (byte)0);
	}

	public SetEntityFlags(Reader reader)
	{
		entityFlags = reader.readByte();
	}

	@Override public boolean differs(MocapStateAction previousAction)
	{
		return entityFlags != ((SetEntityFlags)previousAction).entityFlags;
	}

	public void write(Writer writer, MocapRecordingData data)
	{
		writer.addByte(entityFlags);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		EntityData.ENTITY_FLAGS.set(ctx.getEntity(), entityFlags);
		return Result.OK;
	}
}
