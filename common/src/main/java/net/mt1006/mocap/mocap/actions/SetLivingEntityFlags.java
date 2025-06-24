package net.mt1006.mocap.mocap.actions;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapStateAction;
import net.mt1006.mocap.utils.EntityData;

public class SetLivingEntityFlags implements MocapStateAction
{
	private final byte livingEntityFlags;

	public SetLivingEntityFlags(Entity entity)
	{
		livingEntityFlags = (entity instanceof LivingEntity) ? EntityData.LIVING_ENTITY_FLAGS.valOrDef(entity, (byte)0) : 0;
	}

	public SetLivingEntityFlags(Reader reader)
	{
		livingEntityFlags = reader.readByte();
	}

	@Override public boolean differs(MocapStateAction previousAction)
	{
		return livingEntityFlags != ((SetLivingEntityFlags)previousAction).livingEntityFlags;
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addByte(livingEntityFlags);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (!(ctx.getEntity() instanceof LivingEntity)) { return Result.IGNORED; }
		EntityData.LIVING_ENTITY_FLAGS.set(ctx.getEntity(), livingEntityFlags);
		return Result.OK;
	}
}
