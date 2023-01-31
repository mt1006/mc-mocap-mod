package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.block.BlockState;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

public class PlaceBlock implements BlockAction
{
	private final BlockStateData previousBlockState;
	private final BlockStateData newBlockState;
	private final BlockPos blockPos;

	public PlaceBlock(BlockState previousBlockState, BlockState newBlockState, BlockPos blockPos)
	{
		this.previousBlockState = new BlockStateData(previousBlockState);
		this.newBlockState = new BlockStateData(newBlockState);
		this.blockPos = blockPos;
	}

	public PlaceBlock(RecordingFile.Reader reader)
	{
		previousBlockState = new BlockStateData(reader);
		newBlockState = new BlockStateData(reader);
		blockPos = new BlockPos(reader.readInt(), reader.readInt(), reader.readInt());
	}

	public void write(RecordingFile.Writer writer)
	{
		writer.addByte(PLACE_BLOCK);

		previousBlockState.write(writer);
		newBlockState.write(writer);

		writer.addInt(blockPos.getX());
		writer.addInt(blockPos.getY());
		writer.addInt(blockPos.getZ());
	}

	@Override public void preExecute(FakePlayer fakePlayer, Vector3i blockOffset)
	{
		previousBlockState.placeSilently(fakePlayer, blockPos.offset(blockOffset));
	}

	@Override public int execute(PlayerList packetTargets, FakePlayer fakePlayer, Vector3i blockOffset)
	{
		newBlockState.place(fakePlayer, blockPos.offset(blockOffset));
		return RET_OK;
	}
}
