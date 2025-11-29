package net.mt1006.mocap.mocap.settings;

import net.mt1006.mocap.api.v1.controller.config.MocapDimensionSource;
import net.mt1006.mocap.api.v1.controller.config.MocapEntitiesAfterPlayback;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.controller.config.MocapPlayerNameHandling;
import org.jetbrains.annotations.Nullable;

public class PlaybackConfig implements MocapPlaybackConfig
{
	private @Nullable Boolean canPushEntities;
	private @Nullable MocapEntitiesAfterPlayback entitiesAfterPlayback;
	private @Nullable Boolean blockActionsPlayback;
	private @Nullable Boolean blockInitialization;
	private @Nullable Boolean blockAllowScaled;
	private @Nullable Boolean dropFromBlocks;
	private @Nullable Boolean startAsRecorded;
	private @Nullable Boolean chatPlayback;
	private @Nullable Boolean invulnerablePlayback;
	private @Nullable MocapDimensionSource dimensionSource;
	private @Nullable MocapPlayerNameHandling playerNameHandling;

	public PlaybackConfig(boolean setDefault)
	{
		canPushEntities = setDefault ? Settings.CAN_PUSH_ENTITIES.defVal : null;
		entitiesAfterPlayback = setDefault ? Settings.ENTITIES_AFTER_PLAYBACK.defVal : null;
		blockActionsPlayback = setDefault ? Settings.BLOCK_ACTIONS_PLAYBACK.defVal : null;
		blockInitialization = setDefault ? Settings.BLOCK_INITIALIZATION.defVal : null;
		blockAllowScaled = setDefault ? Settings.BLOCK_ALLOW_SCALED.defVal : null;
		dropFromBlocks = setDefault ? Settings.DROP_FROM_BLOCKS.defVal : null;
		startAsRecorded = setDefault ? Settings.START_AS_RECORDED.defVal : null;
		chatPlayback = setDefault ? Settings.CHAT_PLAYBACK.defVal : null;
		invulnerablePlayback = setDefault ? Settings.INVULNERABLE_PLAYBACK.defVal : null;
		dimensionSource = setDefault ? Settings.DIMENSION_SOURCE.defVal : null;
		playerNameHandling = setDefault ? Settings.PLAYER_NAME_HANDLING.defVal : null;
	}

	private PlaybackConfig(PlaybackConfig toCopy)
	{
		canPushEntities = toCopy.canPushEntities;
		entitiesAfterPlayback = toCopy.entitiesAfterPlayback;
		blockActionsPlayback = toCopy.blockActionsPlayback;
		blockInitialization = toCopy.blockInitialization;
		blockAllowScaled = toCopy.blockAllowScaled;
		dropFromBlocks = toCopy.dropFromBlocks;
		startAsRecorded = toCopy.startAsRecorded;
		chatPlayback = toCopy.chatPlayback;
		invulnerablePlayback = toCopy.invulnerablePlayback;
		dimensionSource = toCopy.dimensionSource;
		playerNameHandling = toCopy.playerNameHandling;
	}

	@Override public MocapPlaybackConfig copy()
	{
		return new PlaybackConfig(this);
	}

	@Override public boolean getCanPushEntities()
	{
		return canPushEntities != null ? canPushEntities : Settings.CAN_PUSH_ENTITIES.val;
	}

	@Override public void setCanPushEntities(@Nullable Boolean val)
	{
		canPushEntities = val;
	}

	@Override public MocapEntitiesAfterPlayback getEntitiesAfterPlayback()
	{
		return entitiesAfterPlayback != null ? entitiesAfterPlayback : Settings.ENTITIES_AFTER_PLAYBACK.val;
	}

	@Override public void setEntitiesAfterPlayback(@Nullable MocapEntitiesAfterPlayback val)
	{
		entitiesAfterPlayback = val;
	}

	@Override public boolean getBlockActionsPlayback()
	{
		return blockActionsPlayback != null ? blockActionsPlayback : Settings.BLOCK_ACTIONS_PLAYBACK.val;
	}

	@Override public void setBlockActionsPlayback(@Nullable Boolean val)
	{
		blockActionsPlayback = val;
	}

	@Override public boolean getBlockInitialization()
	{
		return blockInitialization != null ? blockInitialization : Settings.BLOCK_INITIALIZATION.val;
	}

	@Override public void setBlockInitialization(@Nullable Boolean val)
	{
		blockInitialization = val;
	}

	@Override public boolean getBlockAllowScaled()
	{
		return blockAllowScaled != null ? blockAllowScaled : Settings.BLOCK_ALLOW_SCALED.val;
	}

	@Override public void setBlockAllowScaled(@Nullable Boolean val)
	{
		blockAllowScaled = val;
	}

	@Override public boolean getDropFromBlocks()
	{
		return dropFromBlocks != null ? dropFromBlocks : Settings.DROP_FROM_BLOCKS.val;
	}

	@Override public void setDropFromBlocks(@Nullable Boolean val)
	{
		dropFromBlocks = val;
	}

	@Override public boolean getStartAsRecorded()
	{
		return startAsRecorded != null ? startAsRecorded : Settings.START_AS_RECORDED.val;
	}

	@Override public void setStartAsRecorded(@Nullable Boolean val)
	{
		startAsRecorded = val;
	}

	@Override public boolean getChatPlayback()
	{
		return chatPlayback != null ? chatPlayback : Settings.CHAT_PLAYBACK.val;
	}

	@Override public void setChatPlayback(@Nullable Boolean val)
	{
		chatPlayback = val;
	}

	@Override public boolean getInvulnerablePlayback()
	{
		return invulnerablePlayback != null ? invulnerablePlayback : Settings.INVULNERABLE_PLAYBACK.val;
	}

	@Override public void setInvulnerablePlayback(@Nullable Boolean val)
	{
		invulnerablePlayback = val;
	}

	@Override public MocapDimensionSource getDimensionSource()
	{
		return dimensionSource != null ? dimensionSource : Settings.DIMENSION_SOURCE.val;
	}

	@Override public void setDimensionSource(@Nullable MocapDimensionSource val)
	{
		dimensionSource = val;
	}

	@Override public MocapPlayerNameHandling getPlayerNameHandling()
	{
		return playerNameHandling != null ? playerNameHandling : Settings.PLAYER_NAME_HANDLING.val;
	}

	@Override public void setPlayerNameHandling(@Nullable MocapPlayerNameHandling val)
	{
		playerNameHandling = val;
	}
}
