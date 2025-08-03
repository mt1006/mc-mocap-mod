package net.mt1006.mocap.api.v1.controller.config;

import net.mt1006.mocap.mocap.settings.RecordingConfig;
import org.jetbrains.annotations.Nullable;

public interface MocapRecordingConfig
{
	static MocapRecordingConfig createDefault()
	{
		return new RecordingConfig(true);
	}

	static MocapRecordingConfig createFromSettings()
	{
		return new RecordingConfig(false);
	}

	MocapRecordingConfig copy();

	boolean getPreventTrackingPlayedEntities();

	void setPreventTrackingPlayedEntities(@Nullable Boolean val);

	double getEntityTrackingDistance();

	void setEntityTrackingDistance(@Nullable Double val);

	MocapOnDeath getOnDeath();

	void setOnDeath(@Nullable MocapOnDeath val);

	boolean getAssignPlayerName();

	void setAssignPlayerName(@Nullable Boolean val);

	boolean getChatRecording();

	void setCharRecording(@Nullable Boolean val);
}
