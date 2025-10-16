package net.mt1006.mocap.mocap.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.mt1006.mocap.api.v1.extension.MocapBlockState;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapBasicActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapBlockAction;

public class PlaceBlockSilently extends PlaceBlock
{
	public PlaceBlockSilently(BlockState previousBlockState, BlockState newBlockState, BlockPos blockPos)
	{
		super(previousBlockState, newBlockState, blockPos);
	}

	public PlaceBlockSilently(Reader reader, MocapRecordingData data)
	{
		super(reader, data);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		newBlockState.placeSilently(ctx, blockPos);
		return Result.OK;
	}
}
