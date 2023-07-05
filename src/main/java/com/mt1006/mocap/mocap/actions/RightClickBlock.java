package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

public class RightClickBlock implements BlockAction
{
	private final BlockRayTraceResult blockHitResult;
	private final boolean offHand;

	public RightClickBlock(BlockRayTraceResult blockHitResult, boolean offHand)
	{
		this.blockHitResult = blockHitResult;
		this.offHand = offHand;
	}

	public RightClickBlock(RecordingFiles.Reader reader)
	{
		Vector3d pos = new Vector3d(reader.readDouble(), reader.readDouble(), reader.readDouble());
		BlockPos blockPos = new BlockPos(reader.readInt(), reader.readInt(), reader.readInt());
		Direction direction = directionFromByte(reader.readByte());
		boolean inside = reader.readBoolean();

		blockHitResult = new BlockRayTraceResult(pos, direction, blockPos, inside);
		offHand = reader.readBoolean();
	}

	private Direction directionFromByte(byte val)
	{
		switch (val)
		{
			default: return Direction.DOWN;
			case 1: return Direction.UP;
			case 2: return Direction.NORTH;
			case 3: return Direction.SOUTH;
			case 4: return Direction.WEST;
			case 5: return Direction.EAST;
		}
	}

	private byte directionToByte(Direction direction)
	{
		switch (direction)
		{
			default: return 0;
			case UP: return 1;
			case NORTH: return 2;
			case SOUTH: return 3;
			case WEST: return 4;
			case EAST: return 5;
		}
	}

	public void write(RecordingFiles.Writer writer)
	{
		writer.addByte(Type.RIGHT_CLICK_BLOCK.id);

		writer.addDouble(blockHitResult.getLocation().x);
		writer.addDouble(blockHitResult.getLocation().y);
		writer.addDouble(blockHitResult.getLocation().z);

		writer.addInt(blockHitResult.getBlockPos().getX());
		writer.addInt(blockHitResult.getBlockPos().getY());
		writer.addInt(blockHitResult.getBlockPos().getZ());

		writer.addByte(directionToByte(blockHitResult.getDirection()));
		writer.addBoolean(blockHitResult.isInside());

		writer.addBoolean(offHand);
	}

	@Override public void preExecute(Entity entity, Vector3i blockOffset) {}

	@Override public Result execute(PlayingContext ctx)
	{
		if (!(ctx.entity instanceof PlayerEntity)) { return Result.IGNORED; }

		BlockState blockState = ctx.level.getBlockState(blockHitResult.getBlockPos().offset(ctx.blockOffset));
		Hand interactionHand = offHand ? Hand.OFF_HAND : Hand.MAIN_HAND;

		if (blockState.getBlock() instanceof BedBlock) { return Result.OK; }
		blockState.use(ctx.level, (PlayerEntity)ctx.entity, interactionHand, blockHitResult);
		return Result.OK;
	}
}