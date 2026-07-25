package net.mt1006.mocap.api.v1.events;

import net.minecraft.server.MinecraftServer;
import net.mt1006.mocap.api.v1.controller.MocapPlaybackRoot;
import net.mt1006.mocap.api.v1.controller.playable.MocapActiveRecording;
import net.mt1006.mocap.api.v1.extension.MocapRecordingContext;

public final class MocapEvents
{
	//TODO: add equals() to MocapPlayable
	//TODO: add RECORDING_TICK action?
	private MocapEvents() {}

	/**
	 * Invoked when AFTER server is started, but right before Motion Capture loads files for a world.
	 * Useful if you want to modify mocap files right before world is loaded.
	 * @see ServerStartPre
	 */
	public static final MocapEvent<ServerStartPre> SERVER_START_PRE =
			new MocapEvent<>((listeners) -> (srv) -> listeners.forEach((l) -> l.onServerStartPre(srv)));

	/**
	 * Invoked after server is stared and Motion Capture loads files for a world.
	 * If you want to register controller, this is the point when you should do that.
	 * @see ServerStartPost
	 */
	public static final MocapEvent<ServerStartPost> SERVER_START_POST =
			new MocapEvent<>((listeners) -> (srv) -> listeners.forEach((l) -> l.onServerStartPost(srv)));

	/**
	 * Invoked when server is stopping and right before Motion Capture unloads itself from a world.
	 * Unloading includes stopping all playbacks and recordings.
	 * Useful if you want to gracefully handle stopping recordings or playbacks.
	 * Note that even if Motion Capture will forcefully stop them,
	 * events on stopping will be fired anyway so you may handle it there instead.
	 * @see ServerStopPre
	 */
	public static final MocapEvent<ServerStopPre> SERVER_STOP_PRE =
			new MocapEvent<>((listeners) -> (srv) -> listeners.forEach((l) -> l.onServerStopPre(srv)));

	/**
	 * Invoked when server is stopping and right after Motion Capture unloads itself from a world.
	 * If you want to destroy controller (e.g. set null to handle), this is the point when you should do that.
	 * Note that at this point server is still during stopping, not after it.
	 * @see ServerStopPost
	 */
	public static final MocapEvent<ServerStopPost> SERVER_STOP_POST =
			new MocapEvent<>((listeners) -> (srv) -> listeners.forEach((l) -> l.onServerStopPost(srv)));

	/**
	 * Invoked when recording is initialized - it was started immediately or is waiting for action.
	 * @see RecordingStart
	 */
	public static final MocapEvent<RecordingStart> RECORDING_START =
			new MocapEvent<>((listeners) -> (rec, instant) -> listeners.forEach((l) -> l.onRecordingStart(rec, instant)));

	/**
	 * Invoked when recording is actually started (actions are being recorded).
	 * @see RecordingStartNow
	 */
	public static final MocapEvent<RecordingStartNow> RECORDING_START_NOW =
			new MocapEvent<>((listeners) -> (rec) -> listeners.forEach((l) -> l.onRecordingStartNow(rec)));

	/**
	 * Invoked when state of a recording changes.
	 * @see RecordingChangeState
	 * @see MocapRecordingContext.State
	 */
	public static final MocapEvent<RecordingChangeState> RECORDING_CHANGE_STATE =
			new MocapEvent<>((listeners) -> (rec, prevState) -> listeners.forEach((l) -> l.onRecordingChangeState(rec, prevState)));

	/**
	 * Invoked when playback is started.
	 * @see PlaybackStart
	 */
	public static final MocapEvent<PlaybackStart> PLAYBACK_START =
			new MocapEvent<>((listeners) -> (playback) -> listeners.forEach((l) -> l.onPlaybackStart(playback)));

	/**
	 * Invoked when playback ends. Either because it finished or because it was stopped.
	 * @see PlaybackEnd
	 */
	public static final MocapEvent<PlaybackEnd> PLAYBACK_END =
			new MocapEvent<>((listeners) -> (playback) -> listeners.forEach((l) -> l.onPlaybackEnd(playback)));

	/**
	 * Invoked when player sends request to clear cache.
	 * This event should be used by mods to know when to clear their own cache, if they have one.
	 * @see ClearCache
	 */
	public static final MocapEvent<ClearCache> CLEAR_CACHE =
			new MocapEvent<>((listeners) -> () -> listeners.forEach(ClearCache::onClearCache));

	/**
	 * Invoked when player sends request to refresh command suggestions.
	 * This event should be used by mods to know when to refresh suggestion data in their own commands.
	 * @see ClearCache
	 */
	public static final MocapEvent<RefreshSuggestions> REFRESH_SUGGESTIONS =
			new MocapEvent<>((listeners) -> () -> listeners.forEach(RefreshSuggestions::onRefreshSuggestions));

	@FunctionalInterface
	public interface ServerStartPre
	{
		void onServerStartPre(MinecraftServer server);
	}

	@FunctionalInterface
	public interface ServerStartPost
	{
		void onServerStartPost(MinecraftServer server);
	}

	@FunctionalInterface
	public interface ServerStopPre
	{
		void onServerStopPre(MinecraftServer server);
	}

	@FunctionalInterface
	public interface ServerStopPost
	{
		void onServerStopPost(MinecraftServer server);
	}

	@FunctionalInterface
	public interface RecordingStart
	{
		void onRecordingStart(MocapActiveRecording recording, boolean instantStart);
	}

	@FunctionalInterface
	public interface RecordingStartNow
	{
		void onRecordingStartNow(MocapActiveRecording recording);
	}

	@FunctionalInterface
	public interface RecordingChangeState
	{
		void onRecordingChangeState(MocapActiveRecording recording, MocapRecordingContext.State prevState);
	}

	@FunctionalInterface
	public interface PlaybackStart
	{
		void onPlaybackStart(MocapPlaybackRoot playback);
	}

	@FunctionalInterface
	public interface PlaybackEnd
	{
		void onPlaybackEnd(MocapPlaybackRoot playback);
	}

	@FunctionalInterface
	public interface ClearCache
	{
		void onClearCache();
	}

	@FunctionalInterface
	public interface RefreshSuggestions
	{
		void onRefreshSuggestions();
	}
}
