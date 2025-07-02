package net.mt1006.mocap.mocap.actions;

import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;

public class DummyAction implements MocapAction
{
	public static final DummyAction INSTANCE = new DummyAction();

	private DummyAction() {}

	public static DummyAction fromReader(Reader ignore)
	{
		return INSTANCE;
	}

	@Override public void write(Writer writer, MocapRecordingData data) {}

	@Override public Result execute(MocapActionContext ctx)
	{
		return Result.IGNORED;
	}
}
