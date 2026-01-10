package net.mt1006.mocap.mocap.settings;

import net.mt1006.mocap.api.v1.controller.config.*;
import net.mt1006.mocap.api.v1.modifiers.MocapEntityFilter;
import org.jetbrains.annotations.Nullable;

public class RecordingConfig implements MocapRecordingConfig
{
	private @Nullable MocapEntityFilter trackEntities;
	private @Nullable Boolean preventTrackingPlayedEntities;
	private @Nullable Double entityTrackingDistance;
	private @Nullable MocapOnDeath onDeath;
	private @Nullable MocapOnChangeDimension onChangeDimension;
	private @Nullable Boolean assignDimension;
	private @Nullable MocapAssignProfile assignProfile;
	private @Nullable Boolean chatRecording;
	private @Nullable MocapNbtRecordingMode nbtRecordingMode;

	public RecordingConfig(boolean setDefault)
	{
		trackEntities = setDefault ? Settings.TRACK_ENTITIES.defVal : null;
		preventTrackingPlayedEntities = setDefault ? Settings.PREVENT_TRACKING_PLAYED_ENTITIES.defVal : null;
		entityTrackingDistance = setDefault ? Settings.ENTITY_TRACKING_DISTANCE.defVal : null;
		onDeath = setDefault ? Settings.ON_DEATH.defVal : null;
		onChangeDimension = setDefault ? Settings.ON_CHANGE_DIMENSION.defVal : null;
		assignDimension = setDefault ? Settings.ASSIGN_DIMENSION.defVal : null;
		assignProfile = setDefault ? Settings.ASSIGN_PROFILE.defVal : null;
		chatRecording = setDefault ? Settings.CHAT_RECORDING.defVal : null;
		nbtRecordingMode = setDefault ? Settings.NBT_RECORDING_MODE.defVal : null;
	}

	private RecordingConfig(RecordingConfig toCopy)
	{
		trackEntities = toCopy.trackEntities;
		preventTrackingPlayedEntities = toCopy.preventTrackingPlayedEntities;
		entityTrackingDistance = toCopy.entityTrackingDistance;
		onDeath = toCopy.onDeath;
		onChangeDimension = toCopy.onChangeDimension;
		assignDimension = toCopy.assignDimension;
		assignProfile = toCopy.assignProfile;
		chatRecording = toCopy.chatRecording;
		nbtRecordingMode = toCopy.nbtRecordingMode;
	}

	@Override public MocapRecordingConfig copy()
	{
		return new RecordingConfig(this);
	}

	@Override public MocapEntityFilter getTrackEntities()
	{
		return trackEntities != null ? trackEntities : Settings.TRACK_ENTITIES.val;
	}

	@Override public void setTrackEntities(@Nullable MocapEntityFilter val)
	{
		trackEntities = val;
	}

	@Override public boolean getPreventTrackingPlayedEntities()
	{
		return preventTrackingPlayedEntities != null ? preventTrackingPlayedEntities : Settings.PREVENT_TRACKING_PLAYED_ENTITIES.val;
	}

	@Override public void setPreventTrackingPlayedEntities(@Nullable Boolean val)
	{
		preventTrackingPlayedEntities = val;
	}

	@Override public double getEntityTrackingDistance()
	{
		return entityTrackingDistance != null ? entityTrackingDistance : Settings.ENTITY_TRACKING_DISTANCE.val;
	}

	@Override public void setEntityTrackingDistance(@Nullable Double val)
	{
		entityTrackingDistance = val;
	}

	@Override public MocapOnDeath getOnDeath()
	{
		return onDeath != null ? onDeath : Settings.ON_DEATH.val;
	}

	@Override public void setOnDeath(@Nullable MocapOnDeath val)
	{
		onDeath = val;
	}

	@Override public MocapOnChangeDimension getOnChangeDimension()
	{
		return onChangeDimension != null ? onChangeDimension : Settings.ON_CHANGE_DIMENSION.val;
	}

	@Override public void setOnChangeDimension(@Nullable MocapOnChangeDimension val)
	{
		onChangeDimension = val;
	}

	@Override public boolean getAssignDimension()
	{
		return assignDimension != null ? assignDimension : Settings.ASSIGN_DIMENSION.val;
	}

	@Override public void setAssignDimension(@Nullable Boolean val)
	{
		assignDimension = val;
	}

	@Override public MocapAssignProfile getAssignProfile()
	{
		return assignProfile != null ? assignProfile : Settings.ASSIGN_PROFILE.val;
	}

	@Override public void setAssignProfile(@Nullable MocapAssignProfile val)
	{
		assignProfile = val;
	}

	@Override public boolean getChatRecording()
	{
		return chatRecording != null ? chatRecording : Settings.CHAT_RECORDING.val;
	}

	@Override public void setCharRecording(@Nullable Boolean val)
	{
		chatRecording = val;
	}

	@Override public MocapNbtRecordingMode getNbtRecordingMode()
	{
		return nbtRecordingMode != null ? nbtRecordingMode : Settings.NBT_RECORDING_MODE.val;
	}

	@Override public void setNbtRecordingMode(@Nullable MocapNbtRecordingMode val)
	{
		nbtRecordingMode = val;
	}
}
