package net.mt1006.mocap.mocap.playing;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.command.CommandsContext;
import net.mt1006.mocap.command.io.CommandInfo;
import net.mt1006.mocap.command.io.CommandOutput;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.files.SceneData;
import net.mt1006.mocap.mocap.files.SceneFiles;
import net.mt1006.mocap.mocap.playing.modifiers.PlaybackModifiers;
import net.mt1006.mocap.mocap.playing.playback.Playback;
import net.mt1006.mocap.mocap.playing.playback.PlaybackRoot;
import net.mt1006.mocap.mocap.recording.Recording;
import net.mt1006.mocap.mocap.recording.RecordingContext;
import net.mt1006.mocap.mocap.settings.Settings;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Playing
{
	public static final String MOCAP_ENTITY_TAG = "mocap_entity";
	public static final Multimap<String, PlaybackRoot> playbacksByOwner = HashMultimap.create();
	public static final Collection<PlaybackRoot> playbacks = playbacksByOwner.values();
	private static long tickCounter = 0;
	private static double timer = 0.0;
	private static double previousPlaybackSpeed = 0.0;
	private static int nextPlaybackId = 0;

	public static boolean start(CommandInfo commandInfo, String name, PlaybackModifiers modifiers, boolean sendModifiersWarning)
	{
		if (name.charAt(0) == '-') { return startCurrentlyRecorded(commandInfo, name, modifiers, sendModifiersWarning); }

		PlaybackRoot playback = Playback.start(commandInfo, name, modifiers, getNextId(), false);
		if (playback == null) { return false; }
		addPlayback(playback);
		sendStartMessage(commandInfo, sendModifiersWarning);
		return true;
	}

	public static @Nullable PlaybackRoot startSingleSilently(CommandInfo commandInfo, String name, PlaybackModifiers modifiers, boolean hidden)
	{
		PlaybackRoot playback;
		if (name.charAt(0) == '-')
		{
			Collection<RecordingContext> contexts = Recording.resolveContexts(commandInfo, name);
			if (contexts == null || contexts.size() != 1) { return null; }

			RecordingContext ctx = contexts.iterator().next();
			playback = Playback.start(commandInfo, ctx.data, ctx.id.str, modifiers, getNextId(), hidden);
		}
		else
		{
			playback = Playback.start(commandInfo, name, modifiers, getNextId(), hidden);
		}

		if (playback == null) { return null; }
		addPlayback(playback);
		return playback;
	}

	private static boolean startCurrentlyRecorded(CommandInfo commandInfo, String name, PlaybackModifiers modifiers, boolean sendModifiersWarning)
	{
		Collection<RecordingContext> contexts = Recording.resolveContexts(commandInfo, name);
		if (contexts == null) { return false; }

		int successes = 0;
		for (RecordingContext ctx : contexts)
		{
			PlaybackModifiers modifiersToApply = modifiers;
			if (Settings.START_AS_RECORDED.val)
			{
				PlaybackModifiers playerNameModifier = PlaybackModifiers.empty();
				playerNameModifier.playerName = ctx.recordedPlayer.getName().getString();
				modifiersToApply = modifiers.mergeWithParent(playerNameModifier);
			}

			PlaybackRoot playback = Playback.start(commandInfo, ctx.data, ctx.id.str, modifiersToApply, getNextId(), false);
			if (playback != null)
			{
				addPlayback(playback);
				successes++;
			}
		}

		if (successes == 0) { return false; }
		sendStartMessage(commandInfo, sendModifiersWarning);
		return true;
	}

	private static void sendStartMessage(CommandInfo commandInfo, boolean sendModifiersWarning)
	{
		String key = "playback.start.success";
		if (sendModifiersWarning) { key += ".modifiers"; }

		if (commandInfo.getSourcePlayer() != null)
		{
			CommandsContext commandsContext = CommandsContext.get(commandInfo.getSourcePlayer());
			if (commandsContext.getSync()) { key += ".sync"; }
		}

		commandInfo.sendSuccess(key);
	}

	public static boolean stop(CommandOutput commandOutput, String id, @Nullable String expectedName)
	{
		PlaybackRoot playback = findPlayback(commandOutput, id, expectedName);
		if (playback == null)
		{
			commandOutput.sendFailureWithTip("playback.stop.unable_to_find_playback");
			return false;
		}

		playback.stop();
		commandOutput.sendSuccess("playback.stop.success");
		return true;
	}

	public static @Nullable PlaybackRoot findPlayback(CommandOutput commandOutput, String id, @Nullable String expectedName)
	{
		for (PlaybackRoot playback : playbacks)
		{
			if (playback.getId().equals(id))
			{
				if (expectedName != null && !expectedName.equals(playback.getRootName()))
				{
					commandOutput.sendFailure("playback.stop.wrong_playback_name"); //TODO: FIX
					return null;
				}
				return playback;
			}
		}
		return null;
	}

	public static void stopAll(CommandOutput commandOutput, @Nullable ServerPlayer player)
	{
		if (player == null)
		{
			playbacks.forEach(PlaybackRoot::stop);
			commandOutput.sendSuccess(playbacks.isEmpty() ? "playback.stop_all.empty": "playback.stop_all.all");
		}
		else
		{
			Collection<PlaybackRoot> playerPlaybacks = playbacksByOwner.get(player.getName().getString());
			playerPlaybacks.forEach(PlaybackRoot::stop);

			if (playerPlaybacks.isEmpty())
			{
				commandOutput.sendSuccess(playbacks.isEmpty()
						? "playback.stop_all.empty"
						: "playback.stop_all.own.empty");
			}
			else
			{
				commandOutput.sendSuccess(playerPlaybacks.size() == playbacks.size()
						? "playback.stop_all.own.all"
						: "playback.stop_all.own.not_all");
			}

			if (playerPlaybacks.size() != playbacks.size() && Settings.SHOW_TIPS.val)
			{
				commandOutput.sendSuccess("playback.stop_all.own.tip");
			}
		}
	}

	public static boolean modifiersSet(FullCommandInfo rootCommandInfo)
	{
		ServerPlayer source = rootCommandInfo.getSourcePlayer();
		if (source == null)
		{
			rootCommandInfo.sendFailure("failure.resolve_player");
			return false;
		}
		CommandsContext ctx = CommandsContext.get(source);

		FullCommandInfo commandInfo = rootCommandInfo.getFinalCommandInfo();
		if (commandInfo == null)
		{
			rootCommandInfo.sendFailure("error.unable_to_get_argument");
			return false;
		}

		String propertyName = commandInfo.getNode(4);
		if (propertyName == null)
		{
			rootCommandInfo.sendFailure("error.unable_to_get_argument");
			return false;
		}

		try
		{
			boolean success = ctx.modifiers.modify(commandInfo, propertyName, 4);
			if (!success)
			{
				rootCommandInfo.sendFailure("error.generic");
				return false;
			}

			rootCommandInfo.sendSuccess("playback.modifiers.set");
			return true;
		}
		catch (Exception e)
		{
			rootCommandInfo.sendException(e, "error.unable_to_get_argument");
			return false;
		}
	}

	public static boolean modifiersList(CommandInfo commandInfo)
	{
		ServerPlayer source = commandInfo.getSourcePlayer();
		if (source == null)
		{
			commandInfo.sendFailure("failure.resolve_player");
			return false;
		}

		CommandsContext ctx = CommandsContext.get(source);
		commandInfo.sendSuccess("playback.modifiers.list");
		ctx.modifiers.list(commandInfo);
		return true;
	}

	public static boolean modifiersReset(CommandInfo commandInfo)
	{
		ServerPlayer source = commandInfo.getSourcePlayer();
		if (source == null)
		{
			commandInfo.sendFailure("failure.resolve_player");
			return false;
		}

		CommandsContext ctx = CommandsContext.get(source);
		ctx.modifiers = PlaybackModifiers.empty();
		commandInfo.sendSuccess("playback.modifiers.reset");
		return true;
	}

	public static boolean modifiersAddTo(CommandInfo commandInfo, String sceneName, String toAdd)
	{
		ServerPlayer source = commandInfo.getSourcePlayer();
		if (source == null)
		{
			commandInfo.sendFailure("failure.resolve_player");
			return false;
		}

		SceneData.Subscene subscene = new SceneData.Subscene(toAdd, CommandsContext.get(source).modifiers);
		return SceneFiles.addElement(commandInfo, sceneName, subscene);
	}

	public static boolean list(CommandOutput commandOutput)
	{
		commandOutput.sendSuccess("playback.list");
		playbacks.forEach((p) -> commandOutput.sendSuccessLiteral("[%d] %s", p.getId(), p.getRootName()));
		return true;
	}

	public static void onTick()
	{
		if (playbacks.isEmpty())
		{
			tickCounter++;
			return;
		}

		if (previousPlaybackSpeed != Settings.PLAYBACK_SPEED.val)
		{
			timer = 0.0;
			previousPlaybackSpeed = Settings.PLAYBACK_SPEED.val;
		}

		if ((long)timer < tickCounter) { timer = tickCounter; }

		while ((long)timer == tickCounter)
		{
			List<PlaybackRoot> toRemove = new ArrayList<>();

			for (PlaybackRoot playback : playbacks)
			{
				if (playback.isFinished()) { toRemove.add(playback); }
				else { playback.tick(); }
			}

			removePlaybacks(toRemove);
			if (playbacks.isEmpty()) { break; }

			timer += 1.0 / Settings.PLAYBACK_SPEED.val;
		}
		tickCounter++;
	}

	private static void addPlayback(PlaybackRoot playback)
	{
		ServerPlayer owner = playback.getOwner();
		playbacksByOwner.put(owner != null ? owner.getName().getString() : "", playback);
	}

	private static void removePlaybacks(Collection<PlaybackRoot> toRemove)
	{
		for (PlaybackRoot playback : toRemove)
		{
			ServerPlayer owner = playback.getOwner();
			playbacksByOwner.remove(owner != null ? owner.getName().getString() : "", playback);
		}
	}

	public static int getNextId()
	{
		return nextPlaybackId++;
	}
}
