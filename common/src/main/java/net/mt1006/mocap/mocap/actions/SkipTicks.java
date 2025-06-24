package net.mt1006.mocap.mocap.actions;

import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;

public class SkipTicks implements MocapAction
{
	public final int number;

	public SkipTicks(int number)
	{
		if (number > 255) { throw new RuntimeException("Trying to skip more than 255 ticks!"); }
		this.number = number;
	}

	public SkipTicks(Reader reader)
	{
		this.number = Byte.toUnsignedInt(reader.readByte());
	}

	public boolean canBeModified()
	{
		return number < 255;
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addByte((byte)number);
		//TODO: test
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (ctx.shouldStopRepeat(number)) { return Result.OK; }

		//MocapMod.LOGGER.warn("SKIP TICK (ST/{})", number); //TODO: remove
		ctx.incrementRepeatCounter();
		return Result.REPEAT_TICK;
	}
}
