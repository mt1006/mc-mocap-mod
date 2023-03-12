package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.block.BlockState;
import net.minecraft.server.management.PlayerList;
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

	public RightClickBlock(RecordingFile.Reader reader)
	{
		Vector3d pos = new Vector3d(reader.readDouble(), reader.readDouble(), reader.readDouble());
		BlockPos blockPos = new BlockPos(reader.readInt(), reader.readInt(), reader.readInt());
		Direction direction = directionFromByte(reader.readByte());
		boolean inside = reader.readBoolean();

		blockHitResult = new BlockRayTraceResult(pos, direction, blockPos, inside);
		offHand = reader.readBoolean();
	}

	public void write(RecordingFile.Writer writer)
	{
		writer.addByte(RIGHT_CLICK_BLOCK);

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

	@Override public void preExecute(FakePlayer fakePlayer, Vector3i blockOffset) {}

	@Override public int execute(PlayerList packetTargets, FakePlayer fakePlayer, Vector3i blockOffset)
	{
		BlockState blockState = fakePlayer.level.getBlockState(blockHitResult.getBlockPos().offset(blockOffset));
		Hand interactionHand = offHand ? Hand.OFF_HAND : Hand.MAIN_HAND;

		blockState.use(fakePlayer.level, fakePlayer, interactionHand, blockHitResult);
		return RET_OK;
	}
}