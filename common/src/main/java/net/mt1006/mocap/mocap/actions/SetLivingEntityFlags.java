package net.mt1006.mocap.mocap.actions;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapStateAction;
import net.mt1006.mocap.utils.EntityData;

public class SetLivingEntityFlags implements MocapStateAction
{
	private final byte flags;

	public SetLivingEntityFlags(Entity entity)
	{
		flags = (entity instanceof LivingEntity) ? EntityData.LIVING_ENTITY_FLAGS.valOrDef(entity, (byte)0) : 0;
	}

	public SetLivingEntityFlags(Reader reader)
	{
		flags = reader.readByte();
	}

	@Override public boolean differs(MocapStateAction previousAction)
	{
		return flags != ((SetLivingEntityFlags)previousAction).flags;
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addByte(flags);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (!(ctx.getEntity() instanceof LivingEntity)) { return Result.IGNORED; }
		EntityData.LIVING_ENTITY_FLAGS.set(ctx.getEntity(), flags);
		return Result.OK;
	}
}
