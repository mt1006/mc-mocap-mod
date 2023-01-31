package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockAction extends Action
{
	void preExecute(FakePlayer fakePlayer, Vec3i blockOffset);

	class BlockStateData
	{
		int blockID;

		public BlockStateData(BlockState blockState)
		{
			this.blockID = Block.getId(blockState);
		}

		public BlockStateData(RecordingFile.Reader reader)
		{
			blockID = reader.readInt();
		}

		public void write(RecordingFile.Writer writer)
		{
			writer.addInt(blockID);
		}

		public void place(FakePlayer fakePlayer, BlockPos blockPos)
		{
			BlockState blockState = Block.stateById(blockID);
			Level level = fakePlayer.level;

			level.setBlock(blockPos, blockState, 3);

			SoundType soundType = blockState.getSoundType();
			level.playSound(fakePlayer, blockPos, blockState.getSoundType().getPlaceSound(),
					SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
		}

		public void placeSilently(FakePlayer fakePlayer, BlockPos blockPos)
		{
			fakePlayer.level.setBlock(blockPos, Block.stateById(blockID), 3);
		}
	}
}
