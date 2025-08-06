package net.mt1006.mocap.mocap.actions;

import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;

public class NextTick implements MocapAction
{
	public static final NextTick INSTANCE = new NextTick();

	private NextTick() {}

	@Override public void write(Writer writer, MocapRecordingData data) {}

	@Override public Result execute(MocapActionContext ctx)
	{
		return Result.NEXT_TICK;
	}
}
