package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.players.PlayerList;
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

	public BreakBlock(RecordingFile.Reader reader)
	{
		previousBlockState = new BlockStateData(reader);
		blockPos = new BlockPos(reader.readInt(), reader.readInt(), reader.readInt());
	}

	public void write(RecordingFile.Writer writer)
	{
		writer.addByte(BREAK_BLOCK);

		previousBlockState.write(writer);

		writer.addInt(blockPos.getX());
		writer.addInt(blockPos.getY());
		writer.addInt(blockPos.getZ());
	}

	@Override public void preExecute(FakePlayer fakePlayer, Vec3i blockOffset)
	{
		previousBlockState.placeSilently(fakePlayer, blockPos.offset(blockOffset));
	}

	@Override public int execute(PlayerList packetTargets, FakePlayer fakePlayer, Vec3i blockOffset)
	{
		fakePlayer.level.destroyBlock(blockPos.offset(blockOffset), false, fakePlayer);
		return RET_OK;
	}
}
