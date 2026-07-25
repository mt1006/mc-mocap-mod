package net.mt1006.mocap.api.v1.controller.playable;

import net.mt1006.mocap.api.v1.extension.MocapRecordingContext;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import org.jetbrains.annotations.Nullable;

public interface MocapActiveRecording extends MocapPlayable
{
	/**
	 * Equivalent of running "playback stop".
	 * @param out log output
	 * @return true if succeeded, false otherwise
	 */
	boolean stop(CommandOutput out);

	/**
	 * Equivalent of running "playback discard".
	 * @param out log output
	 * @return true if succeeded, false otherwise
	 */
	boolean discard(CommandOutput out);

	/**
	 * Equivalent of running "playback save".
	 * @param out log output
	 * @param name name of recording file to be saved as (like "save" command argument, not filename with extension)
	 * @return reference to saved recording file if succeeded, null otherwise
	 */
	@Nullable MocapRecordingFile save(CommandOutput out, String name);

	/**
	 * Get context of a recording, allowing to interact with recording on a lower level.
	 * @return context of a recording
	 * @see MocapRecordingContext
	 */
	MocapRecordingContext getContext();
}
