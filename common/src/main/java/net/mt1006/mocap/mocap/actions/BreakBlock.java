package net.mt1006.mocap.mocap.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.mt1006.mocap.api.v1.extension.MocapBlockState;
import net.mt1006.mocap.api.v1.extension.MocapPositionTransformer;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapBlockAction;
import net.mt1006.mocap.mocap.settings.Settings;

import java.util.List;

public class BreakBlock implements MocapBlockAction
{
	private final MocapBlockState previousBlockState;
	private final BlockPos blockPos;

	public BreakBlock(BlockState blockState, BlockPos blockPos)
	{
		this.previousBlockState = new BlockStateData(blockState);
		this.blockPos = blockPos;
	}

	public BreakBlock(Reader reader, MocapRecordingData data)
	{
		previousBlockState = new BlockStateData(reader, data);
		blockPos = reader.readBlockPos();
	}

	@Override public void prepareWrite(MocapRecordingData data)
	{
		previousBlockState.prepareWrite(data);
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		previousBlockState.write(writer);
		writer.addBlockPos(blockPos);
	}

	@Override public void preExecute(Entity entity, MocapPositionTransformer transformer)
	{
		previousBlockState.placeSilently(entity, transformer, blockPos);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		List<? extends BlockPos> blocks = ctx.getTransformer().transformBlockPos(blockPos);
		blocks.forEach((b) -> ctx.getLevel().destroyBlock(b, Settings.DROP_FROM_BLOCKS.val));
		return Result.OK;
	}
}
