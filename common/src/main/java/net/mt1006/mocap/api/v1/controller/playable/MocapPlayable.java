package net.mt1006.mocap.api.v1.controller.playable;

import net.mt1006.mocap.api.v1.controller.MocapPlaybackRoot;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.mocap.playing.PlaybackDataManager;
import net.mt1006.mocap.mocap.playing.playable.ActiveRecording;
import net.mt1006.mocap.mocap.playing.playable.RecordingFile;
import net.mt1006.mocap.mocap.playing.playable.SceneFile;
import net.mt1006.mocap.mocap.playing.playback.Playback;
import net.mt1006.mocap.mocap.playing.playback.PositionTransformer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface MocapPlayable
{
	static @Nullable MocapPlayable get(CommandInfo info, String name)
	{
		return switch (name.charAt(0))
		{
			case '.' -> SceneFile.get(info, name);
			case '-' -> ActiveRecording.get(info, name);
			default -> RecordingFile.get(info, name);
		};
	}

	String getName();

	boolean exists();

	default @Nullable MocapPlaybackRoot startPlayback(CommandInfo info, MocapModifiers modifiers)
	{
		return startPlayback(info, modifiers, MocapPlaybackConfig.createFromSettings(), true);
	}

	@Nullable MocapPlaybackRoot startPlayback(CommandInfo info, MocapModifiers modifiers, MocapPlaybackConfig config, boolean isHidden);

	@ApiStatus.Internal
	@Nullable Playback startAsSubscene(CommandInfo info, MocapModifiers modifiers, MocapPlaybackConfig config,
									   PlaybackDataManager dataManager, PositionTransformer parentTransformer);
}
