package net.mt1006.mocap.api.v1.extension;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapBasicActionContext;
import net.mt1006.mocap.mocap.actions.BlockStateData;

public interface MocapBlockState
{
	static MocapBlockState create(BlockState blockState)
	{
		return new BlockStateData(blockState);
	}

	BlockState get();

	void prepareWrite(MocapRecordingData data);

	void write(MocapAction.Writer writer);

	void place(MocapBasicActionContext ctx, BlockPos blockPos);

	void placeSilently(MocapBasicActionContext ctx, BlockPos blockPos);
}
