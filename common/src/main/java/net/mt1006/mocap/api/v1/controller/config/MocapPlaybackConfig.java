package net.mt1006.mocap.api.v1.controller.config;

import net.mt1006.mocap.mocap.settings.PlaybackConfig;
import org.jetbrains.annotations.Nullable;

public interface MocapPlaybackConfig
{
	static MocapPlaybackConfig createDefault()
	{
		return new PlaybackConfig(true);
	}

	static MocapPlaybackConfig createFromSettings()
	{
		return new PlaybackConfig(false);
	}

	MocapPlaybackConfig copy();

	boolean getCanPushEntities();

	void setCanPushEntities(@Nullable Boolean val);

	MocapEntitiesAfterPlayback getEntitiesAfterPlayback();

	void setEntitiesAfterPlayback(@Nullable MocapEntitiesAfterPlayback val);

	boolean getBlockActionsPlayback();

	void setBlockActionsPlayback(@Nullable Boolean val);

	boolean getBlockInitialization();

	void setBlockInitialization(@Nullable Boolean val);

	boolean getBlockAllowScaled();

	void setBlockAllowScaled(@Nullable Boolean val);

	boolean getDropFromBlocks();

	void setDropFromBlocks(@Nullable Boolean val);

	boolean getStartAsRecorded();

	void setStartAsRecorded(@Nullable Boolean val);

	boolean getChatPlayback();

	void setChatPlayback(@Nullable Boolean val);

	boolean getInvulnerablePlayback();

	void setInvulnerablePlayback(@Nullable Boolean val);

	MocapDimensionSource getDimensionSource();

	void setDimensionSource(@Nullable MocapDimensionSource val);
}
