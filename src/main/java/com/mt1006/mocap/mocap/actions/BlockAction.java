package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;

public interface BlockAction extends Action
{
	void preExecute(FakePlayer fakePlayer, Vector3i blockOffset);

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
			World level = fakePlayer.level;

			level.setBlock(blockPos, blockState, 3);

			SoundType soundType = blockState.getSoundType(level, blockPos, fakePlayer);
			level.playSound(fakePlayer, blockPos, blockState.getSoundType(level, blockPos, fakePlayer).getPlaceSound(),
					SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
		}

		public void placeSilently(FakePlayer fakePlayer, BlockPos blockPos)
		{
			fakePlayer.level.setBlock(blockPos, Block.stateById(blockID), 3);
		}
	}
}
