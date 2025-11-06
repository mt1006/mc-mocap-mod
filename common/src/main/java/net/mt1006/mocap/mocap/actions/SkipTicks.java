package net.mt1006.mocap.mocap.actions;

import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapTickAction;

public class SkipTicks implements MocapTickAction
{
	private final int number;

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

	public SkipTicks increment()
	{
		return new SkipTicks(number + 1);
	}

	@Override public int getTickCount()
	{
		return number;
	}

	@Override public boolean endsTick()
	{
		return number != 0;
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addByte((byte)number);
		//TODO: test
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (ctx.shouldStopRepeat(number)) { return Result.OK; }

		ctx.incrementRepeatCounter();
		return Result.REPEAT_TICK;
	}
}
