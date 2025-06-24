package net.mt1006.mocap.mocap.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.mt1006.mocap.api.v1.extension.MocapBlockState;
import net.mt1006.mocap.api.v1.extension.MocapPositionTransformer;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;

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

	@Override public void prepareWrite(MocapRecordingData data)
	{
		idToWrite = data.provideBlockStateId(blockState);
	}

	@Override public void write(MocapAction.Writer writer)
	{
		if (idToWrite == -1) { throw new RuntimeException("BlockStateData write wasn't prepared!"); }
		writer.addInt(idToWrite);
	}

	@Override public void place(Entity entity, MocapPositionTransformer transformer, BlockPos blockPos)
	{
		BlockState finBlockState = transformer.transformBlockState(blockState);
		transformer.transformBlockPos(blockPos).forEach((b) -> placeSingle(entity, b, finBlockState));
	}

	@Override public void placeSilently(Entity entity, MocapPositionTransformer transformer, BlockPos blockPos)
	{
		BlockState finBlockState = transformer.transformBlockState(blockState);
		transformer.transformBlockPos(blockPos).forEach((b) -> placeSingleSilently(entity, b, finBlockState));
	}

	private static void placeSingle(Entity entity, BlockPos blockPos, BlockState blockState)
	{
		Level level = entity.level();

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

	private static void placeSingleSilently(Entity entity, BlockPos blockPos, BlockState blockState)
	{
		entity.level().setBlock(blockPos, blockState, 3);
	}
}
