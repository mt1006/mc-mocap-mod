package net.mt1006.mocap.mocap.actions;

import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapTickAction;

public class NextTick implements MocapTickAction
{
	public static final NextTick INSTANCE = new NextTick();

	private NextTick() {}

	@Override public int getTickCount()
	{
		return 1;
	}

	@Override public boolean endsTick()
	{
		return true;
	}

	@Override public void write(Writer writer, MocapRecordingData data) {}

	@Override public Result execute(MocapActionContext ctx)
	{
		return Result.NEXT_TICK;
	}
}
