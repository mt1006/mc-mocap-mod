package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class SetArrowCount implements ComparableAction
{
	private final int arrowCount;
	private final int beeStingerCount;

	public SetArrowCount(Entity entity)
	{
		if (entity instanceof LivingEntity)
		{
			arrowCount = ((LivingEntity)entity).getArrowCount();
			beeStingerCount = ((LivingEntity)entity).getStingerCount();
		}
		else
		{
			arrowCount = 0;
			beeStingerCount = 0;
		}
	}

	public SetArrowCount(RecordingFiles.Reader reader)
	{
		arrowCount = reader.readInt();
		beeStingerCount = reader.readInt();
	}

	@Override public boolean differs(ComparableAction action)
	{
		return arrowCount != ((SetArrowCount)action).arrowCount ||
				beeStingerCount != ((SetArrowCount)action).beeStingerCount;
	}

	@Override public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action)
	{
		if (action != null && !differs(action)) { return; }
		writer.addByte(Type.SET_ARROW_COUNT.id);

		writer.addInt(arrowCount);
		writer.addInt(beeStingerCount);
	}

	@Override public Result execute(PlayingContext ctx)
	{
		if (!(ctx.entity instanceof LivingEntity)) { return Result.IGNORED; }
		((LivingEntity)ctx.entity).setArrowCount(arrowCount);
		((LivingEntity)ctx.entity).setStingerCount(beeStingerCount);
		return Result.OK;
	}
}
