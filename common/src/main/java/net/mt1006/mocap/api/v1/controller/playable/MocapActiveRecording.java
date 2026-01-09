package net.mt1006.mocap.api.v1.controller.playable;

import net.mt1006.mocap.api.v1.extension.MocapRecordingContext;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import org.jetbrains.annotations.Nullable;

public interface MocapActiveRecording extends MocapPlayable
{
	boolean stop(CommandOutput out);

	boolean discard(CommandOutput out);

	@Nullable MocapRecordingFile save(CommandOutput out, String name);

	MocapRecordingContext getContext();

	boolean isValid();
}
