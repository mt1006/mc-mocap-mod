package net.mt1006.mocap.mocap.actions;

import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.utils.FakePlayer;

public class Die implements MocapAction
{
	public static final Die INSTANCE = new Die();

	private Die() {}

	public static Die fromReader(Reader ignore)
	{
		return INSTANCE;
	}

	@Override public void write(Writer writer, MocapRecordingData data) {}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (ctx.getEntity() instanceof FakePlayer) { ((FakePlayer)ctx.getEntity()).fakeKill(); }
		else { ctx.getEntity().kill(ctx.getLevel()); }
		return Result.OK;
	}
}
