package net.mt1006.mocap.api.v1.controller.playable;

import net.mt1006.mocap.api.v1.extension.MocapActiveRecordingActions;
import org.jetbrains.annotations.Nullable;

public interface MocapActiveRecording extends MocapPlayable
{
	boolean stop();

	boolean discard();

	@Nullable MocapSavedRecording save(String name);

	MocapActiveRecordingActions getActions();
}
