package net.mt1006.mocap.api.v1.extension.actions;

public interface MocapBlockAction extends MocapAction
{
	/**
	 * Called at playback start to initialize blocks
	 * - return them to state at the beginning of the recording.
	 * Whenever these methods will be called on block actions depends on value
	 * of "block_initialization" setting/configuration. It's not defined whenever
	 * changing its value during block initialization will stop it or not
	 * (as of writing this, it doesn't).
	 * <p>
	 * Currently these methods aren't called if they're packed in ENTITY_ACTION.
	 * This may change in the future.
	 * @param ctx initial (limited) action context
	 * @see net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig#setBlockInitialization(Boolean)
	 */
	void initBlocks(MocapBasicActionContext ctx);
}
