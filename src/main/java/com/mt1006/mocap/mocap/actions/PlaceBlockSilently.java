package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public class PlaceBlockSilently implements BlockAction
{
	private final BlockStateData previousBlockState;
	private final BlockStateData newBlockState;
	private final BlockPos blockPos;

	public PlaceBlockSilently(BlockState previousBlockState, BlockState newBlockState, BlockPos blockPos)
	{
		this.previousBlockState = new BlockStateData(previousBlockState);
		this.newBlockState = new BlockStateData(newBlockState);
		this.blockPos = blockPos;
	}

	public PlaceBlockSilently(RecordingFiles.Reader reader)
	{
		previousBlockState = new BlockStateData(reader);
		newBlockState = new BlockStateData(reader);
		blockPos = new BlockPos(reader.readInt(), reader.readInt(), reader.readInt());
	}

	public void write(RecordingFiles.Writer writer)
	{
		writer.addByte(Type.PLACE_BLOCK_SILENTLY.id);

		previousBlockState.write(writer);
		newBlockState.write(writer);

		writer.addInt(blockPos.getX());
		writer.addInt(blockPos.getY());
		writer.addInt(blockPos.getZ());
	}

	@Override public void preExecute(Entity entity, Vec3i blockOffset)
	{
		previousBlockState.placeSilently(entity, blockPos.offset(blockOffset));
	}

	@Override public Result execute(PlayingContext ctx)
	{
		newBlockState.placeSilently(ctx.entity, blockPos.offset(ctx.blockOffset));
		return Result.OK;
	}
}
