package net.mt1006.mocap.mocap.actions;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapStateAction;
import org.jetbrains.annotations.Nullable;

public class SetArrowCount implements MocapStateAction
{
	private final int arrowCount;
	private final int beeStingerCount;

	public static @Nullable SetArrowCount fromEntity(Entity entity)
	{
		return (entity instanceof LivingEntity livingEntity) ? new SetArrowCount(livingEntity) : null;
	}

	private SetArrowCount(LivingEntity entity)
	{
		arrowCount = entity.getArrowCount();
		beeStingerCount = entity.getStingerCount();
	}

	public SetArrowCount(Reader reader)
	{
		arrowCount = reader.readInt();
		beeStingerCount = reader.readInt();
	}

	@Override public boolean differs(MocapStateAction previousAction)
	{
		return arrowCount != ((SetArrowCount)previousAction).arrowCount
				|| beeStingerCount != ((SetArrowCount)previousAction).beeStingerCount;
	}

	@Override public boolean shouldBeInitialized()
	{
		return arrowCount != 0 || beeStingerCount != 0;
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addInt(arrowCount);
		writer.addInt(beeStingerCount);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (!(ctx.getEntity() instanceof LivingEntity)) { return Result.IGNORED; }
		((LivingEntity)ctx.getEntity()).setArrowCount(arrowCount);
		((LivingEntity)ctx.getEntity()).setStingerCount(beeStingerCount);
		return Result.OK;
	}
}
