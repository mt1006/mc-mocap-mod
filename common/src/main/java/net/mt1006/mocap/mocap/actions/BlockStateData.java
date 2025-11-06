package net.mt1006.mocap.mocap.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.mt1006.mocap.api.v1.extension.MocapBlockState;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapBasicActionContext;

public class BlockStateData implements MocapBlockState
{
	private final BlockState blockState;
	private int idToWrite = -1;

	public BlockStateData(BlockState blockState)
	{
		this.blockState = blockState;
	}

	public BlockStateData(MocapAction.Reader reader, MocapRecordingData data)
	{
		blockState = data.blockStateFromId(reader.readInt());
	}

	@Override public BlockState get()
	{
		return blockState;
	}

	@Override public void prepareWrite(MocapRecordingData data)
	{
		idToWrite = data.provideBlockStateId(blockState);
	}

	@Override public void write(MocapAction.Writer writer)
	{
		if (idToWrite == -1) { throw new RuntimeException("BlockStateData write wasn't prepared!"); }
		writer.addInt(idToWrite);
	}

	@Override public void place(MocapBasicActionContext ctx, BlockPos blockPos)
	{
		Entity entity = ctx.getEntity();
		Level level = ctx.getLevel();
		BlockState finBlockState = ctx.getTransformer().transformBlockState(blockState);
		ctx.getTransformer().transformBlockPos(blockPos, ctx.getConfig().getBlockAllowScaled())
				.forEach((block) -> placeSingle(entity, level, block, finBlockState));
	}

	@Override public void placeSilently(MocapBasicActionContext ctx, BlockPos blockPos)
	{
		Level level = ctx.getLevel();
		BlockState finBlockState = ctx.getTransformer().transformBlockState(blockState);
		ctx.getTransformer().transformBlockPos(blockPos, ctx.getConfig().getBlockAllowScaled())
				.forEach((block) -> placeSingleSilently(level, block, finBlockState));
	}

	private static void placeSingle(Entity entity, Level level, BlockPos blockPos, BlockState blockState)
	{
		if (blockState.isAir())
		{
			level.destroyBlock(blockPos, true);
		}
		else
		{
			level.setBlock(blockPos, blockState, 3);

			SoundType soundType = blockState.getSoundType();
			level.playSound(entity, blockPos, blockState.getSoundType().getPlaceSound(),
					SoundSource.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f);
		}
	}

	private static void placeSingleSilently(Level level, BlockPos blockPos, BlockState blockState)
	{
		level.setBlock(blockPos, blockState, 3);
	}
}
