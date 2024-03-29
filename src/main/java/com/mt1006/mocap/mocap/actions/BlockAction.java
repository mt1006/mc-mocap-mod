package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockAction extends Action
{
	void preExecute(Entity entity, Vec3i blockOffset);

	class BlockStateData
	{
		int blockID;

		public BlockStateData(BlockState blockState)
		{
			this.blockID = Block.getId(blockState);
		}

		public BlockStateData(RecordingFiles.Reader reader)
		{
			blockID = reader.readInt();
		}

		public void write(RecordingFiles.Writer writer)
		{
			writer.addInt(blockID);
		}

		public void place(Entity entity, BlockPos blockPos)
		{
			BlockState blockState = Block.stateById(blockID);
			Level level = entity.level();

			if (blockState.isAir())
			{
				level.destroyBlock(blockPos, true);
			}
			else
			{
				level.setBlock(blockPos, blockState, 3);

				SoundType soundType = blockState.getSoundType(level, blockPos, entity);
				level.playSound(entity, blockPos, blockState.getSoundType(level, blockPos, entity).getPlaceSound(),
						SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
			}
		}

		public void placeSilently(Entity entity, BlockPos blockPos)
		{
			entity.level().setBlock(blockPos, Block.stateById(blockID), 3);
		}
	}
}
