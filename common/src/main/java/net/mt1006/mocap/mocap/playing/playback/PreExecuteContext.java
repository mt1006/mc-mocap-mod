package net.mt1006.mocap.mocap.playing.playback;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.extension.MocapPositionTransformer;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapBasicActionContext;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;

public class PreExecuteContext implements MocapBasicActionContext
{
	private final MocapRecordingData recordingData;
	private final Entity entity;
	private final ServerLevel level;
	private final MocapPlaybackConfig config;
	private final MocapModifiers modifiers;
	private final MocapPositionTransformer transformer;

	public PreExecuteContext(MocapRecordingData recordingData, Entity entity, ServerLevel level,
							 MocapPlaybackConfig config, MocapModifiers modifiers, MocapPositionTransformer transformer)
	{
		this.recordingData = recordingData;
		this.entity = entity;
		this.level = level;
		this.config = config;
		this.modifiers = modifiers;
		this.transformer = transformer;
	}

	@Override public MocapRecordingData getRecordingData()
	{
		return recordingData;
	}

	@Override public Entity getEntity()
	{
		return entity;
	}

	@Override public ServerLevel getLevel()
	{
		return level;
	}

	@Override public MocapPlaybackConfig getConfig()
	{
		return config;
	}

	@Override public MocapModifiers getModifiers()
	{
		return modifiers;
	}

	@Override public MocapPositionTransformer getTransformer()
	{
		return transformer;
	}
}
