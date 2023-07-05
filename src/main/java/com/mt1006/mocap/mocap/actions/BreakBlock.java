package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import com.mt1006.mocap.mocap.settings.Settings;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

public class BreakBlock implements BlockAction
{
	private final BlockStateData previousBlockState;
	private final BlockPos blockPos;

	public BreakBlock(BlockState blockState, BlockPos blockPos)
	{
		this.previousBlockState = new BlockStateData(blockState);
		this.blockPos = blockPos;
	}

	public BreakBlock(RecordingFiles.Reader reader)
	{
		previousBlockState = new BlockStateData(reader);
		blockPos = new BlockPos(reader.readInt(), reader.readInt(), reader.readInt());
	}

	public void write(RecordingFiles.Writer writer)
	{
		writer.addByte(Type.BREAK_BLOCK.id);

		previousBlockState.write(writer);

		writer.addInt(blockPos.getX());
		writer.addInt(blockPos.getY());
		writer.addInt(blockPos.getZ());
	}

	@Override public void preExecute(Entity entity, Vector3i blockOffset)
	{
		previousBlockState.placeSilently(entity, blockPos.offset(blockOffset));
	}

	@Override public Result execute(PlayingContext ctx)
	{
		ctx.level.destroyBlock(blockPos.offset(ctx.blockOffset), Settings.DROP_FROM_BLOCKS.val);
		return Result.OK;
	}
}
