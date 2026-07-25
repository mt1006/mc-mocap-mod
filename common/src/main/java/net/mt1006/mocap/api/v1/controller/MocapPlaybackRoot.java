package net.mt1006.mocap.api.v1.controller;

import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import org.jetbrains.annotations.Nullable;

public interface MocapPlaybackRoot
{
	/**
	 * Get unique playback ID. Playback ID should be unique throughout server lifespan.
	 * As of version 1.4 this is integer in a string form, but this may change.
	 * @return playback ID
	 */
	String getId();

	/**
	 * Get valid playback ID, which is intended to be used as a suggestions.
	 * As of version 1.4 it has format "%03d-%s", where %03d is ID, and %s root name.
	 * This format is intended to improve sorting and give player information about
	 * playback root name. Note that this format may change, but this ID should
	 * always be a valid ID, that is it can be used to find playback root.
	 * If you want to get ID and/or get root name, you should use methods
	 * made for this purpose.
	 * @return playback ID for a suggestion
	 * @see MocapPlaybackRoot#getId()
	 * @see MocapPlaybackRoot#getRootName()
	 */
	String getSuggestedId();

	/**
	 * Returns root name of a playback, that is name of scene, recording, or
	 * other playable, that was used to start playback.
	 * @return root name
	 */
	String getRootName();

	@Nullable ServerPlayer getOwner();

	MocapPlaybackConfig getConfig();

	boolean isHidden();

	boolean isFinished();

	void stop();
}
