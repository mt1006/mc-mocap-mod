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
	/**
	 * When using API, use {@link net.mt1006.mocap.api.v1.controller.MocapController#getPlayable(String) MocapController.getPlayable(name)} instead
	 */
	@ApiStatus.Internal
	static @Nullable MocapPlayable get(CommandInfo info, String name)
	{
		if (name == null || name.isEmpty())
		{
			return null;
		}	
		return switch (name.charAt(0))
		{
			case '.' -> SceneFile.get(info, name);
			case '-' -> ActiveRecording.get(info, name);
			default -> RecordingFile.get(info, name);
		};
	}

	/**
	 * Returns name of a playable object, which can be referenced by player with command,
	 * or by other mod with call to getPlayable().
	 * @return name of playable object
	 */
	String getName();

	/**
	 * Determines if playable file/object referenced by instance of this class still exists.
	 * That means whenever it can be playback back or not. If it exists, it doesn't mean it can
	 * be referenced by player or retrieve by name with API. For this use accessible() instead.
	 * @return true if exists, false otherwise
	 * @see MocapPlayable#accessible()
	 */
	boolean exists();

	/**
	 * Determines if playable object/file referenced by instance of this class is accessible by player or controller API.
	 * If this method returns false, but exitst() returns true, it means playable object is no longer referenced
	 * by Motion Capture (it was marked as removed), but it still exists in memory, and therefore can be played back.
	 * @return true if accessible, otherwise false
	 * @see	MocapPlayable#exists()
	 */
	boolean accessible();

	/**
	 * @see MocapPlayable#startPlayback(CommandInfo, MocapModifiers, MocapPlaybackConfig, boolean)
	 */
	default @Nullable MocapPlaybackRoot startPlayback(CommandInfo info, MocapModifiers modifiers)
	{
		return startPlayback(info, modifiers, MocapPlaybackConfig.createFromSettings(), true);
	}

	/**
	 * Starts playback of a playable object or file referenced by it.
	 * @param info log output and context in which command is executed
	 * @param modifiers playback modifiers to be used
	 * @param config playback configuration (set of setting values related to playback)
	 * @param isHidden should existence of playback be hidden from player (as in inside commands output)
	 * @return playback root if succeeded, null otherwise
	 */
	@Nullable MocapPlaybackRoot startPlayback(CommandInfo info, MocapModifiers modifiers, MocapPlaybackConfig config, boolean isHidden);

	@ApiStatus.Internal
	@Nullable Playback startAsSubscene(CommandInfo info, MocapModifiers modifiers, MocapPlaybackConfig config,
									   PlaybackDataManager dataManager, PositionTransformer parentTransformer);
}
