package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

public class BreakBlockProgress implements BlockAction
{
	private final BlockPos blockPos;
	private final int progress;

	public BreakBlockProgress(BlockPos blockPos, int progress)
	{
		this.blockPos = blockPos;
		this.progress = progress;
	}

	public BreakBlockProgress(RecordingFiles.Reader reader)
	{
		this.blockPos = new BlockPos(reader.readInt(), reader.readInt(), reader.readInt());
		this.progress = reader.readInt();
	}

	public void write(RecordingFiles.Writer writer)
	{
		writer.addByte(Type.BREAK_BLOCK_PROGRESS.id);

		writer.addInt(blockPos.getX());
		writer.addInt(blockPos.getY());
		writer.addInt(blockPos.getZ());
		writer.addInt(progress);
	}

	@Override public void preExecute(Entity entity, Vector3i blockOffset) {}

	@Override public Result execute(PlayingContext ctx)
	{
		ctx.level.destroyBlockProgress(ctx.entity.getId(), blockPos.offset(ctx.blockOffset), progress);
		return Result.OK;
	}
}
