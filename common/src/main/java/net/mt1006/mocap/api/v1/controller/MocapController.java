package net.mt1006.mocap.api.v1.controller;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.controller.config.MocapRecordingConfig;
import net.mt1006.mocap.api.v1.controller.playable.MocapActiveRecording;
import net.mt1006.mocap.api.v1.controller.playable.MocapPlayable;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MocapController
{
	/**
	 * Get log and command context for controller. Context level is overworld.
	 * @return log and command context
	 */
	CommandInfo getCommandInfo();

	/**
	 * Get log and command context for controller. Context level is given as an argument.
	 * @param level level of a command context
	 * @return log and command context
	 */
	CommandInfo getCommandInfoForLevel(ServerLevel level);

	/**
	 * Returns instance of MocapPlayable. Type of playable depends on name prefix,
	 * which follow same convention as when names in commands:
	 * <ol>
	 *     <li>name - recording file</li>
	 *     <li>.name - scene file</li>
	 *     <li>-name - active recording</li>
	 * </ol>
	 * For recording and scene files it should always return non-null reference,
	 * as long as name is valid, so you need to call exists() or accessible()
	 * to check whenever returned object actually exists or is playable.
	 * Naming rules (after parsing prefix):
	 * <ol>
	 *     <li>it cannot be empty</li>
	 *     <li>it cannot start with '.' or '-' - it is part of a prefix</li>
	 *     <li>it may only consist of lowercase ASCII letters, digits, underscores, dashes, and dots</li>
	 * </ol>
	 * @param name name of a playable
	 * @return playable on success, null otherwise
	 */
	@Nullable MocapPlayable getPlayable(String name);

	//TODO: better description here
	/**
	 * Find playback by its ID.
	 * @param id ID of a playback
	 * @return playback root if found, null otherwise
	 */
	@Nullable MocapPlaybackRoot findPlayback(String id);

	/**
	 * List of all currently active playbacks.
	 * @return list of playback roots
	 */
	List<? extends MocapPlaybackRoot> getActivePlaybacks();

	/**
	 * Start recording specific player. Unlike "recording start" command it starts recording immediately.
	 * @param player player to be recorded
	 * @return instance of active recording, or null if something went wrong
	 * @see MocapController#startRecording(ServerPlayer, MocapRecordingConfig, boolean)
	 */
	@Nullable MocapActiveRecording startRecording(ServerPlayer player);

	/**
	 * Start recording specific player.
	 * @param player player to be recorded
	 * @param config configuration (set of setting values) to use by this recording
	 * @param startInstantly start recording immediately, without waiting for player action
	 * @return instance of active recording, or null if something went wrong
	 */
	@Nullable MocapActiveRecording startRecording(ServerPlayer player, MocapRecordingConfig config, boolean startInstantly);

	/**
	 * Get value of a setting. Setting names are not stable interface and may
	 * change in future releases, so you should handle errors gracefully.
	 * @param name name of a setting (this doesn't include setting category)
	 * @return string value of a setting, or null if such setting doesn't exist
	 */
	@Nullable String getSetting(String name);

	/**
	 * Set value of a setting. Setting names are not stable interface and may
	 * change in future releases, so you should handle errors gracefully.
	 * <b>If you want to change settings for a recording or a playback you
	 * should instead use MocapRecordingConfig or MocapPlaybackConfig.</b>
	 * @param name name of a setting (this doesn't include setting category)
	 * @param val new value of a setting in a string form
	 * @return true on success, false otherwise
	 * @see MocapRecordingConfig
	 * @see MocapPlaybackConfig
	 * @see MocapController#startRecording(ServerPlayer, MocapRecordingConfig, boolean)
	 * @see MocapPlayable#startPlayback(CommandInfo, MocapModifiers, MocapPlaybackConfig, boolean)
	 */
	boolean setSetting(String name, String val);

	/**
	 * Reset setting to a default value. Setting names are not stable interface and may
	 * change in future releases, so you should handle errors gracefully.
	 * @param name name of a setting (this doesn't include setting category)
	 * @return true on success, false otherwise
	 */
	boolean resetSetting(String name);
}
