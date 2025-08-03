package net.mt1006.mocap.mocap.playing.playback;

import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.api.v1.controller.MocapPlaybackRoot;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import org.jetbrains.annotations.Nullable;

public class PlaybackRoot implements MocapPlaybackRoot
{
	private final Playback instance;
	private final String id; //TODO: turn them into string ids? (1.5)
	private final String name;
	private final String suggestedId;
	private final MocapPlaybackConfig config;
	private final boolean isHidden;

	public PlaybackRoot(Playback instance, int id, String name, MocapPlaybackConfig config, boolean isHidden)
	{
		this.instance = instance;
		this.id = String.valueOf(id);
		this.name = name;
		this.suggestedId = String.format("%03d-%s", id, name);
		this.config = config;
		this.isHidden = isHidden;
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
		return isHidden;
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
