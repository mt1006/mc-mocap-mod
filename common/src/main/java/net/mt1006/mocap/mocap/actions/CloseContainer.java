package net.mt1006.mocap.mocap.actions;

import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapBasicActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapBlockAction;

public class CloseContainer implements MocapBlockAction
{
	public static final CloseContainer INSTANCE = new CloseContainer();

	private CloseContainer() {}

	@Override public void initBlocks(MocapBasicActionContext ctx) {}

	@Override public void write(Writer writer, MocapRecordingData data) {}

	@Override public Result execute(MocapActionContext ctx)
	{
		ServerPlayer player = ctx.getRealOrDummyPlayer();
		if (player == null) { return Result.IGNORED; }

		player.doCloseContainer();
		return Result.OK;
	}
}
