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

	MocapOnChangeDimension getOnChangeDimension();

	void setOnChangeDimension(@Nullable MocapOnChangeDimension val);

	boolean getAssignDimension();

	void setAssignDimension(@Nullable Boolean val);

	MocapAssignProfile getAssignProfile();

	void setAssignProfile(@Nullable MocapAssignProfile val);

	boolean getChatRecording();

	void setCharRecording(@Nullable Boolean val);

	MocapNbtRecordingMode getNbtRecordingMode();

	void setNbtRecordingMode(@Nullable MocapNbtRecordingMode val);
}
