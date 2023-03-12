package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RightClickBlock implements BlockAction
{
	private final BlockHitResult blockHitResult;
	private final boolean offHand;

	public RightClickBlock(BlockHitResult blockHitResult, boolean offHand)
	{
		this.blockHitResult = blockHitResult;
		this.offHand = offHand;
	}

	public RightClickBlock(RecordingFile.Reader reader)
	{
		Vec3 pos = new Vec3(reader.readDouble(), reader.readDouble(), reader.readDouble());
		BlockPos blockPos = new BlockPos(reader.readInt(), reader.readInt(), reader.readInt());
		Direction direction = directionFromByte(reader.readByte());
		boolean inside = reader.readBoolean();

		blockHitResult = new BlockHitResult(pos, direction, blockPos, inside);
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

	@Override public void preExecute(FakePlayer fakePlayer, Vec3i blockOffset) {}

	@Override public int execute(PlayerList packetTargets, FakePlayer fakePlayer, Vec3i blockOffset)
	{
		BlockState blockState = fakePlayer.level.getBlockState(blockHitResult.getBlockPos().offset(blockOffset));
		InteractionHand interactionHand = offHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;

		blockState.use(fakePlayer.level, fakePlayer, interactionHand, blockHitResult);
		return RET_OK;
	}
}