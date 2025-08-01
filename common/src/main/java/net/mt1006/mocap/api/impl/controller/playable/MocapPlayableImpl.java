package net.mt1006.mocap.api.impl.controller.playable;

import net.mt1006.mocap.api.impl.controller.MocapControllerImpl;
import net.mt1006.mocap.api.v1.controller.MocapPlayback;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.controller.playable.MocapPlayable;
import net.mt1006.mocap.mocap.playing.PlaybackManager;
import net.mt1006.mocap.mocap.playing.modifiers.PlaybackModifiers;
import org.jetbrains.annotations.Nullable;

public abstract class MocapPlayableImpl implements MocapPlayable
{
	protected final MocapControllerImpl ctrl;

	public MocapPlayableImpl(MocapControllerImpl ctrl)
	{
		this.ctrl = ctrl;
	}

	public static MocapPlayable fromName(MocapControllerImpl ctrl, String name)
	{
		return switch (name.charAt(0))
		{
			case '.' -> new MocapSceneImpl(ctrl, name);
			case '-' -> new MocapActiveRecordingImpl(ctrl, name);
			default -> new MocapSavedRecordingImpl(ctrl, name);
		};
	}

	@Override public @Nullable MocapPlayback startPlayback(String playerName)
	{
		return startPlayback(playerName, MocapPlaybackConfig.createFromSettings());
	}

	@Override public @Nullable MocapPlayback startPlayback(String playerName, MocapPlaybackConfig config)
	{
		//TODO: set playerName
		return PlaybackManager.startSingleSilently(ctrl.commandInfo, getId(), config, PlaybackModifiers.empty(), ctrl.hideStuff);
	}
}
