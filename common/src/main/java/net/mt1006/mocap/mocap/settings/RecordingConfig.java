package net.mt1006.mocap.mocap.settings;

import net.mt1006.mocap.api.v1.controller.config.MocapOnDeath;
import net.mt1006.mocap.api.v1.controller.config.MocapRecordingConfig;
import org.jetbrains.annotations.Nullable;

public class RecordingConfig implements MocapRecordingConfig
{
	private @Nullable Boolean preventTrackingPlayedEntities;
	private @Nullable Double entityTrackingDistance;
	private @Nullable MocapOnDeath onDeath;
	private @Nullable Boolean assignPlayerName;
	private @Nullable Boolean chatRecording;

	public RecordingConfig(boolean setDefault)
	{
		preventTrackingPlayedEntities = setDefault ? Settings.PREVENT_TRACKING_PLAYED_ENTITIES.defVal : null;
		entityTrackingDistance = setDefault ? Settings.ENTITY_TRACKING_DISTANCE.defVal : null;
		onDeath = setDefault ? (MocapOnDeath)Settings.ON_DEATH.defVal : null;
		assignPlayerName = setDefault ? Settings.ASSIGN_PLAYER_NAME.defVal : null;
		chatRecording = setDefault ? Settings.CHAT_RECORDING.defVal : null;
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
		return onDeath != null ? onDeath : (MocapOnDeath)Settings.ON_DEATH.val;
	}

	@Override public void setOnDeath(@Nullable MocapOnDeath val)
	{
		onDeath = val;
	}

	@Override public boolean getAssignPlayerName()
	{
		return assignPlayerName != null ? assignPlayerName : Settings.ASSIGN_PLAYER_NAME.val;
	}

	@Override public void setAssignPlayerName(@Nullable Boolean val)
	{
		assignPlayerName = val;
	}

	@Override public boolean getChatRecording()
	{
		return chatRecording != null ? chatRecording : Settings.CHAT_RECORDING.val;
	}

	@Override public void setCharRecording(@Nullable Boolean val)
	{
		chatRecording = val;
	}
}
