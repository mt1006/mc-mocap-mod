package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import com.mt1006.mocap.mocap.settings.Settings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

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

	@Override public void preExecute(Entity entity, Vec3i blockOffset)
	{
		previousBlockState.placeSilently(entity, blockPos.offset(blockOffset));
	}

	@Override public Result execute(PlayingContext ctx)
	{
		ctx.level.destroyBlock(blockPos.offset(ctx.blockOffset), Settings.DROP_FROM_BLOCKS.val);
		return Result.OK;
	}
}
