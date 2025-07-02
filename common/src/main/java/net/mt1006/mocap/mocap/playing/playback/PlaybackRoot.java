package net.mt1006.mocap.mocap.playing.playback;

import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.api.v1.controller.MocapPlayback;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import org.jetbrains.annotations.Nullable;

public class PlaybackRoot implements MocapPlayback
{
	private final Playback instance;
	private final String id; //TODO: turn them into string ids? (1.5)
	private final String name;
	private final String suggestedId;
	private final MocapPlaybackConfig config;
	private final boolean hideId;

	public PlaybackRoot(Playback instance, int id, String name, MocapPlaybackConfig config, boolean hideId)
	{
		this.instance = instance;
		this.id = String.valueOf(id);
		this.name = name;
		this.suggestedId = String.format("%03d-%s", id, name);
		this.config = config;
		this.hideId = hideId;
	}

	@Override public String getId()
	{
		return id;
	}

	@Override public String getSuggestedId()
	{
		return suggestedId;
	}

	@Override public String getRootName()
	{
		return name;
	}

	@Override public @Nullable ServerPlayer getOwner()
	{
		return instance.owner;
	}

	@Override public MocapPlaybackConfig getConfig()
	{
		return config;
	}

	@Override public boolean isHidden()
	{
		return hideId;
	}

	@Override public boolean isFinished()
	{
		return instance.finished;
	}

	@Override public void stop()
	{
		if (!instance.finished) { instance.stop(); }
	}

	public void tick()
	{
		instance.tick();
	}
}
