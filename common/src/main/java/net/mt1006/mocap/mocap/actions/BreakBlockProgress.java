package net.mt1006.mocap.mocap.actions;

import net.minecraft.core.BlockPos;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapBasicActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapBlockAction;

import java.util.List;

public class BreakBlockProgress implements MocapBlockAction
{
	private final BlockPos blockPos;
	private final int progress;

	public BreakBlockProgress(BlockPos blockPos, int progress)
	{
		this.blockPos = blockPos;
		this.progress = progress;
	}

	public BreakBlockProgress(Reader reader)
	{
		this.blockPos = reader.readBlockPos();
		this.progress = reader.readInt();
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addBlockPos(blockPos);
		writer.addInt(progress);
	}

	@Override public void preExecute(MocapBasicActionContext ctx) {}

	@Override public Result execute(MocapActionContext ctx)
	{
		List<? extends BlockPos> blocks = ctx.getTransformer().transformBlockPos(blockPos, ctx.getConfig().getBlockAllowScaled());
		if (!blocks.isEmpty()) { ctx.getLevel().destroyBlockProgress(ctx.getEntity().getId(), blocks.get(0), progress); }
		return Result.OK;
	}
}
