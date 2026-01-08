package net.mt1006.mocap.mocap.playing.playable;

import net.mt1006.mocap.api.v1.controller.MocapPlaybackRoot;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.controller.playable.MocapActiveRecording;
import net.mt1006.mocap.api.v1.controller.playable.MocapRecordingFile;
import net.mt1006.mocap.api.v1.extension.MocapActiveRecordingActions;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.mocap.files.RecordingData;
import net.mt1006.mocap.mocap.playing.PlaybackDataManager;
import net.mt1006.mocap.mocap.playing.PlaybackManager;
import net.mt1006.mocap.mocap.playing.playback.Playback;
import net.mt1006.mocap.mocap.playing.playback.PositionTransformer;
import net.mt1006.mocap.mocap.playing.playback.RecordingPlayback;
import net.mt1006.mocap.mocap.recording.RecordingContext;
import net.mt1006.mocap.mocap.recording.RecordingManager;
import org.jetbrains.annotations.Nullable;

public class ActiveRecording extends Playable implements MocapActiveRecording
{
	public final String id;
	public final RecordingContext ctx;

	private ActiveRecording(String id, RecordingContext ctx)
	{
		this.id = id;
		this.ctx = ctx;
	}

	public static ActiveRecording get(RecordingContext ctx)
	{
		return new ActiveRecording(ctx.id.str, ctx);
	}

	public static @Nullable ActiveRecording get(CommandInfo info, String name)
	{
		RecordingContext ctx = RecordingManager.resolveSingle(info, name);
		return ctx != null ? new ActiveRecording(name, ctx) : null;
	}

	@Override public String getName()
	{
		return id;
	}

	@Override public boolean exists()
	{
		return true;
	}

	@Override public boolean stop(CommandOutput out)
	{
		return ctx != null && RecordingManager.stopSingle(out, ctx, null);
	}

	@Override public boolean discard(CommandOutput out)
	{
		return ctx != null && RecordingManager.discardSingle(out, ctx);
	}

	@Override public @Nullable MocapRecordingFile save(CommandOutput out, String name)
	{
		boolean success = ctx != null && RecordingManager.saveSingle(out, ctx, name, false);
		return success ? RecordingFile.get(out, name) : null;
	}

	@Override public MocapActiveRecordingActions getActions()
	{
		return ctx;
	}

	@Override public boolean isValid()
	{
		return !ctx.isRemoved();
	}

	@Override public RecordingData getRecordingData()
	{
		return ctx.data;
	}

	@Override public @Nullable MocapPlaybackRoot startPlayback(CommandInfo info, MocapModifiers modifiers, MocapPlaybackConfig config, boolean isHidden)
	{
		if (config.getStartAsRecorded() && modifiers.getPlayerName() == null)
		{
			modifiers = modifiers.withPlayerName(ctx.recordedPlayer.getName().getString());
		}

		RecordingPlayback playback = RecordingPlayback.start(info, true, new PlaybackDataManager(), ctx.data, config, modifiers, null);
		return playback != null ? PlaybackManager.onStart(this, playback, isHidden) : null;
	}

	@Override public @Nullable Playback startAsSubscene(CommandInfo info, MocapModifiers modifiers, MocapPlaybackConfig config,
														PlaybackDataManager dataManager, PositionTransformer parentTransformer)
	{
		return RecordingPlayback.start(info, false, dataManager, ctx.data, config, modifiers, parentTransformer);
	}
}
