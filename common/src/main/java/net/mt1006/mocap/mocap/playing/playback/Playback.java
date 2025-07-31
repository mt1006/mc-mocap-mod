package net.mt1006.mocap.mocap.playing.playback;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.command.CommandsContext;
import net.mt1006.mocap.command.io.CommandInfo;
import net.mt1006.mocap.mocap.files.RecordingData;
import net.mt1006.mocap.mocap.files.SceneData;
import net.mt1006.mocap.mocap.playing.DataManager;
import net.mt1006.mocap.mocap.playing.modifiers.PlaybackModifiers;
import net.mt1006.mocap.mocap.recording.Recording;
import net.mt1006.mocap.mocap.recording.RecordingContext;
import org.jetbrains.annotations.Nullable;

public abstract class Playback
{
	//TODO: "playback.start.error.loop" and "playback.start.error.load" as failures, not errors

	protected final boolean root;
	protected final ServerLevel level;
	public final @Nullable ServerPlayer owner;
	public final MocapPlaybackConfig config;
	protected boolean finished = false;
	protected final PlaybackModifiers modifiers;
	protected int tickCounter = 0; //TODO: StartContext?

	public static @Nullable PlaybackRoot start(CommandInfo info, String name, MocapPlaybackConfig config,
											   PlaybackModifiers modifiers, int id, boolean hideId)
	{
		DataManager dataManager = new DataManager();
		if (!dataManager.load(info, name))
		{
			if (!dataManager.knownError) { info.sendFailure("playback.start.error.load"); }
			info.sendFailure("playback.start.error.load.path", dataManager.getResourcePath());
			return null;
		}

		Playback playback = switch (SceneType.fromName(name))
		{
			case RECORDING -> RecordingPlayback.startRoot(info, dataManager.getRecording(name), config, modifiers);
			case SCENE -> ScenePlayback.startRoot(info, dataManager, config, name, modifiers);
		};
		return playback != null ? new PlaybackRoot(playback, id, name, config, hideId) : null;
	}

	public static @Nullable PlaybackRoot start(CommandInfo info, RecordingData recordingData, String name,
											   MocapPlaybackConfig config, PlaybackModifiers modifiers, int id, boolean hideId)
	{
		Playback playback = RecordingPlayback.startRoot(info, recordingData, config, modifiers);
		return playback != null ? new PlaybackRoot(playback, id, name, config, hideId) : null;
	}

	protected static @Nullable Playback start(CommandInfo info, DataManager dataManager,
											  MocapPlaybackConfig config, Playback parent, SceneData.Subscene subscene)
	{
		String name = subscene.name;
		return switch (SceneType.fromName(name))
		{
			case RECORDING -> RecordingPlayback.startSubscene(info, dataManager, config, parent, subscene);
			case SCENE -> ScenePlayback.startSubscene(info, dataManager, config, parent, subscene);
		};
	}

	protected Playback(boolean root, ServerLevel level, @Nullable ServerPlayer owner, MocapPlaybackConfig config,
					   PlaybackModifiers parentModifiers, @Nullable SceneData.Subscene subscene)
	{
		this.root = root;
		this.level = level;
		this.owner = owner;
		this.config = config;

		if (root)
		{
			if (subscene != null) { throw new RuntimeException(); }
			this.modifiers = parentModifiers;
		}
		else
		{
			if (subscene == null) { throw new RuntimeException(); }
			this.modifiers = subscene.modifiers.mergeWithParent(parentModifiers);
		}
	}

	public abstract boolean tick();

	public abstract void stop();

	//TODO: remove?
	public abstract boolean wasFinished();

	protected abstract PositionTransformer getPosTransformer();

	protected boolean shouldExecuteTick()
	{
		if (tickCounter == 0) { return true; }

		if (modifiers.startDelay.ticks <= tickCounter)
		{
			if (CommandsContext.haveSyncEnabled == 0 || owner == null) { return true; }

			CommandsContext commandsContext = CommandsContext.get(owner);
			if (!commandsContext.getSync()) { return true; }

			for (RecordingContext ctx : Recording.bySourcePlayer(owner))
			{
				if (ctx.state == RecordingContext.State.RECORDING) { return true; }
			}
		}
		return false;
	}

	public static class StartException extends Exception {}

	private enum SceneType
	{
		SCENE,
		RECORDING;

		public static SceneType fromName(String name)
		{
			return name.charAt(0) == '.' ? SCENE : RECORDING;
		}
	}
}
