package net.mt1006.mocap.api.v1.controller.playable;

import net.mt1006.mocap.api.v1.extension.MocapActiveRecordingActions;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.mocap.files.RecordingData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface MocapActiveRecording extends MocapPlayable
{
	boolean stop(CommandOutput out);

	boolean discard(CommandOutput out);

	@Nullable MocapRecordingFile save(CommandOutput out, String name);

	MocapActiveRecordingActions getActions();

	boolean isValid();

	@ApiStatus.Internal
	RecordingData getRecordingData();
}
