package net.mt1006.mocap.api.v1.events;

import net.mt1006.mocap.api.v1.controller.MocapPlaybackRoot;
import net.mt1006.mocap.api.v1.controller.playable.MocapActiveRecording;
import net.mt1006.mocap.mocap.recording.RecordingContext;

public final class MocapEvents
{
	//TODO: replace MocapActiveRecordingActions with MocapRecordingContext
	//TODO: add equals() to MocapPlayable
	//TODO: add RECORDING_TICK action?
	private MocapEvents() {}

	public static final MocapEvent<RecordingStart> RECORDING_START =
			new MocapEvent<>((listeners) -> (recording, instantStart) -> listeners.forEach((l) -> l.onRecordingStart(recording, instantStart)));

	public static final MocapEvent<RecordingStartNow> RECORDING_START_NOW =
			new MocapEvent<>((listeners) -> (recording) -> listeners.forEach((l) -> l.onRecordingStartNow(recording)));

	public static final MocapEvent<RecordingChangeState> RECORDING_CHANGE_STATE =
			new MocapEvent<>((listeners) -> (recording, prevState) -> listeners.forEach((l) -> l.onRecordingChangeState(recording, prevState)));

	public static final MocapEvent<PlaybackStart> PLAYBACK_START =
			new MocapEvent<>((listeners) -> (playback) -> listeners.forEach((l) -> l.onPlaybackStart(playback)));

	public static final MocapEvent<PlaybackEnd> PLAYBACK_END =
			new MocapEvent<>((listeners) -> (playback) -> listeners.forEach((l) -> l.onPlaybackEnd(playback)));

	public static final MocapEvent<ClearCache> CLEAR_CACHE =
			new MocapEvent<>((listeners) -> () -> listeners.forEach(ClearCache::onClearCache));

	public static final MocapEvent<RefreshSuggestions> REFRESH_SUGGESTIONS =
			new MocapEvent<>((listeners) -> () -> listeners.forEach(RefreshSuggestions::onRefreshSuggestions));

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
		//TODO: move State into MocapRecordingContext
		void onRecordingChangeState(MocapActiveRecording recording, RecordingContext.State prevState);
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
