package net.mt1006.mocap.mocap.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.mt1006.mocap.api.v1.extension.MocapPositionTransformer;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
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

	@Override public void preExecute(Entity entity, MocapPositionTransformer transformer) {}

	@Override public Result execute(MocapActionContext ctx)
	{
		List<? extends BlockPos> blocks = ctx.getTransformer().transformBlockPos(blockPos);
		if (!blocks.isEmpty()) { ctx.getLevel().destroyBlockProgress(ctx.getEntity().getId(), blocks.get(0), progress); }
		return Result.OK;
	}
}
