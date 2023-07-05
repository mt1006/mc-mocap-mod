package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;

public interface BlockAction extends Action
{
	void preExecute(Entity entity, Vector3i blockOffset);

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
			World level = entity.level;

			if (blockState.isAir())
			{
				level.destroyBlock(blockPos, true);
			}
			else
			{
				level.setBlock(blockPos, blockState, 3);

				if (!(entity instanceof PlayerEntity)) { return; }
				SoundType soundType = blockState.getSoundType(level, blockPos, entity);
				level.playSound((PlayerEntity)entity, blockPos, blockState.getSoundType(level, blockPos, entity).getPlaceSound(),
						SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
			}
		}

		public void placeSilently(Entity entity, BlockPos blockPos)
		{
			entity.level.setBlock(blockPos, Block.stateById(blockID), 3);
		}
	}
}
