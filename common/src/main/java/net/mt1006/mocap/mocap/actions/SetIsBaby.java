package net.mt1006.mocap.mocap.actions;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapStateAction;

public class SetIsBaby implements MocapStateAction
{
	public static final SetIsBaby TRUE = new SetIsBaby(true);
	public static final SetIsBaby FALSE = new SetIsBaby(false);
	private final boolean isBaby;

	public static SetIsBaby fromEntity(Entity entity)
	{
		return (entity instanceof AgeableMob ageableMob && ageableMob.isBaby()) ? TRUE : FALSE;
	}

	public static SetIsBaby read(Reader reader)
	{
		return reader.readBoolean() ? TRUE : FALSE;
	}

	private SetIsBaby(boolean isBaby)
	{
		this.isBaby = isBaby;
	}

	@Override public boolean differs(MocapStateAction previousAction)
	{
		return isBaby != ((SetIsBaby)previousAction).isBaby;
	}

	@Override public boolean shouldBeInitialized()
	{
		return isBaby;
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addBoolean(isBaby);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (ctx.getEntity() instanceof AgeableMob ageableMob)
		{
			ageableMob.setBaby(isBaby);
		}
		return Result.IGNORED;
	}
}
