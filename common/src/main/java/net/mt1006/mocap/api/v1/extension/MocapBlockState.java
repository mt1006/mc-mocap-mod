package net.mt1006.mocap.api.v1.extension;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapBasicActionContext;
import net.mt1006.mocap.mocap.actions.BlockStateData;

/**
 * Holder of BlockState intended to optimize size of saved action block states.
 * They need to be prepared to write when prepareWrite() is called on action.
 */
public interface MocapBlockState
{
	/**
	 * Create unprepared to writing BlockState holder
	 * @param blockState block state to hold
	 * @return created holder
	 */
	static MocapBlockState create(BlockState blockState)
	{
		return new BlockStateData(blockState);
	}

	/**
	 * @return held block state
	 */
	BlockState get();

	/**
	 * Prepare block state to be written. This should be called during MocapAction.prepareWrite().
	 * @param data recording data instance (also argument of MocapAction.prepareWrite())
	 * @see MocapAction#prepareWrite(MocapRecordingData)
	 */
	void prepareWrite(MocapRecordingData data);

	/**
	 * Write (save) block state. Holder should be prepared to be written.
	 * This method will throw exception, if holder isn't prepared.
	 * @param writer writer to be used for writing
	 * @see MocapBlockState#prepareWrite(MocapRecordingData)
	 * @see	MocapAction#write(MocapAction.Writer, MocapRecordingData)
	 */
	void write(MocapAction.Writer writer);

	/**
	 * Place block with given block state at a specific position.
	 * This method will first destroy previous block and emit sound when placing new one.
	 * @param ctx basic action context to be used
	 * @param blockPos position for block to be placed on
	 */
	void place(MocapBasicActionContext ctx, BlockPos blockPos);

	/**
	 * Place block with given block state at a specific position.
	 * This method will replace previous block at given position with new one, without emitting any sound.
	 * @param ctx basic action context to be used
	 * @param blockPos position for block to be placed on
	 */
	void placeSilently(MocapBasicActionContext ctx, BlockPos blockPos);
}
