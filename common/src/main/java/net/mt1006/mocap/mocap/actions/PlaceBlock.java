package net.mt1006.mocap.mocap.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.mt1006.mocap.api.v1.extension.MocapBlockState;
import net.mt1006.mocap.api.v1.extension.MocapPositionTransformer;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapBlockAction;

public class PlaceBlock implements MocapBlockAction
{
	private final MocapBlockState previousBlockState;
	private final MocapBlockState newBlockState;
	private final BlockPos blockPos;

	public PlaceBlock(BlockState previousBlockState, BlockState newBlockState, BlockPos blockPos)
	{
		this.previousBlockState = new BlockStateData(previousBlockState);
		this.newBlockState = new BlockStateData(newBlockState);
		this.blockPos = blockPos;
	}

	public PlaceBlock(Reader reader, MocapRecordingData data)
	{
		previousBlockState = new BlockStateData(reader, data);
		newBlockState = new BlockStateData(reader, data);
		blockPos = reader.readBlockPos();
	}

	@Override public void prepareWrite(MocapRecordingData data)
	{
		previousBlockState.prepareWrite(data);
		newBlockState.prepareWrite(data);
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		previousBlockState.write(writer);
		newBlockState.write(writer);

		writer.addBlockPos(blockPos);
	}

	@Override public void preExecute(Entity entity, MocapPositionTransformer transformer)
	{
		previousBlockState.placeSilently(entity, transformer, blockPos);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		newBlockState.place(ctx.getEntity(), ctx.getTransformer(), blockPos);
		return Result.OK;
	}
}
