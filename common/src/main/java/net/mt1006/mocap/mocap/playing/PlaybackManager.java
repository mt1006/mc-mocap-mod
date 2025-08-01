package net.mt1006.mocap.mocap.playing;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
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

public class PlaybackManager
{
	public static final String MOCAP_ENTITY_TAG = "mocap_entity";
	public static final Multimap<String, PlaybackRoot> playbacksByOwner = HashMultimap.create();
	public static final Collection<PlaybackRoot> playbacks = playbacksByOwner.values();
	private static long tickCounter = 0;
	private static double timer = 0.0;
	private static double previousPlaybackSpeed = 0.0;
	private static int nextPlaybackId = 0;

	public static boolean start(CommandInfo info, String name, MocapPlaybackConfig config,
								PlaybackModifiers modifiers, boolean sendModifiersWarning)
	{
		if (name.charAt(0) == '-') { return startCurrentlyRecorded(info, name, config, modifiers, sendModifiersWarning); }

		PlaybackRoot playback = Playback.start(info, name, config, modifiers, getNextId(), false);
		if (playback == null) { return false; }
		addPlayback(playback);
		sendStartMessage(info, sendModifiersWarning);
		return true;
	}

	public static @Nullable PlaybackRoot startSingleSilently(CommandInfo info, String name, MocapPlaybackConfig config,
															 PlaybackModifiers modifiers, boolean hidden)
	{
		PlaybackRoot playback;
		if (name.charAt(0) == '-')
		{
			Collection<RecordingContext> contexts = Recording.resolveContexts(info, name);
			if (contexts == null || contexts.size() != 1) { return null; }

			RecordingContext ctx = contexts.iterator().next();
			playback = Playback.start(info, ctx.data, ctx.id.str, config, modifiers, getNextId(), hidden);
		}
		else
		{
			playback = Playback.start(info, name, config, modifiers, getNextId(), hidden);
		}

		if (playback == null) { return null; }
		addPlayback(playback);
		return playback;
	}

	private static boolean startCurrentlyRecorded(CommandInfo info, String name, MocapPlaybackConfig config,
												  PlaybackModifiers modifiers, boolean sendModifiersWarning)
	{
		Collection<RecordingContext> contexts = Recording.resolveContexts(info, name);
		if (contexts == null) { return false; }

		int successes = 0;
		for (RecordingContext ctx : contexts)
		{
			PlaybackModifiers modifiersToApply = modifiers;
			if (config.getStartAsRecorded())
			{
				PlaybackModifiers playerNameModifier = PlaybackModifiers.empty();
				playerNameModifier.playerName = ctx.recordedPlayer.getName().getString();
				modifiersToApply = modifiers.mergeWithParent(playerNameModifier);
			}

			PlaybackRoot playback = Playback.start(info, ctx.data, ctx.id.str, config, modifiersToApply, getNextId(), false);
			if (playback != null)
			{
				addPlayback(playback);
				successes++;
			}
		}

		if (successes == 0) { return false; }
		sendStartMessage(info, sendModifiersWarning);
		return true;
	}

	private static void sendStartMessage(CommandInfo info, boolean sendModifiersWarning)
	{
		String key = "playback.start.success";
		if (sendModifiersWarning) { key += ".modifiers"; }

		if (info.getSourcePlayer() != null)
		{
			CommandsContext commandsContext = CommandsContext.get(info.getSourcePlayer());
			if (commandsContext.getSync()) { key += ".sync"; }
		}

		info.sendSuccess(key);
	}

	public static boolean stop(CommandOutput out, String id, @Nullable String expectedName)
	{
		PlaybackRoot playback = findPlayback(out, id, expectedName);
		if (playback == null) { return out.sendFailureWithTip("playback.stop.unable_to_find_playback"); }

		playback.stop();
		out.sendSuccess("playback.stop.success");
		return true;
	}

	public static @Nullable PlaybackRoot findPlayback(CommandOutput out, String id, @Nullable String expectedName)
	{
		for (PlaybackRoot playback : playbacks)
		{
			if (playback.getId().equals(id))
			{
				if (expectedName != null && !expectedName.equals(playback.getRootName()))
				{
					out.sendFailure("playback.stop.wrong_playback_name"); //TODO: FIX
					return null;
				}
				return playback;
			}
		}
		return null;
	}

	public static void stopAll(CommandOutput out, @Nullable ServerPlayer player)
	{
		if (player == null)
		{
			playbacks.forEach(PlaybackRoot::stop);
			out.sendSuccess(playbacks.isEmpty() ? "playback.stop_all.empty": "playback.stop_all.all");
		}
		else
		{
			Collection<PlaybackRoot> playerPlaybacks = playbacksByOwner.get(player.getName().getString());
			playerPlaybacks.forEach(PlaybackRoot::stop);

			if (playerPlaybacks.isEmpty())
			{
				out.sendSuccess(playbacks.isEmpty()
						? "playback.stop_all.empty"
						: "playback.stop_all.own.empty");
			}
			else
			{
				out.sendSuccess(playerPlaybacks.size() == playbacks.size()
						? "playback.stop_all.own.all"
						: "playback.stop_all.own.not_all");
			}

			if (playerPlaybacks.size() != playbacks.size() && Settings.SHOW_TIPS.val)
			{
				out.sendSuccess("playback.stop_all.own.tip");
			}
		}
	}

	public static boolean modifiersSet(FullCommandInfo rootInfo)
	{
		ServerPlayer source = rootInfo.getSourcePlayer();
		if (source == null) { return rootInfo.sendFailure("failure.resolve_player"); }
		CommandsContext ctx = CommandsContext.get(source);

		FullCommandInfo info = rootInfo.getFinalCommandInfo();
		if (info == null) { return rootInfo.sendFailure("error.unable_to_get_argument"); }

		String propertyName = info.getNode(4);
		if (propertyName == null) { return rootInfo.sendFailure("error.unable_to_get_argument"); }

		try
		{
			boolean success = ctx.modifiers.modify(info, propertyName, 4);
			return success ? rootInfo.sendSuccess("playback.modifiers.set") : rootInfo.sendFailure("error.generic");
		}
		catch (Exception e) { return rootInfo.sendException(e, "error.unable_to_get_argument"); }
	}

	public static boolean modifiersList(CommandInfo info)
	{
		ServerPlayer source = info.getSourcePlayer();
		if (source == null) { return info.sendFailure("failure.resolve_player"); }

		CommandsContext ctx = CommandsContext.get(source);
		info.sendSuccess("playback.modifiers.list");
		ctx.modifiers.list(info);
		return true;
	}

	public static boolean modifiersReset(CommandInfo info)
	{
		ServerPlayer source = info.getSourcePlayer();
		if (source == null) { return info.sendFailure("failure.resolve_player"); }

		CommandsContext ctx = CommandsContext.get(source);
		ctx.modifiers = PlaybackModifiers.empty();
		return info.sendSuccess("playback.modifiers.reset");
	}

	public static boolean modifiersAddTo(CommandInfo info, String sceneName, String toAdd)
	{
		ServerPlayer source = info.getSourcePlayer();
		if (source == null) { return info.sendFailure("failure.resolve_player"); }

		SceneData.Subscene subscene = new SceneData.Subscene(toAdd, CommandsContext.get(source).modifiers);
		return SceneFiles.addElement(info, sceneName, subscene);
	}

	public static boolean list(CommandOutput out)
	{
		if (playbacks.isEmpty())
		{
			out.sendSuccess("playback.list.empty");
		}
		else
		{
			out.sendSuccess("playback.list");
			playbacks.forEach((p) -> out.sendSuccessLiteral("[%s] %s", p.getId(), p.getRootName()));
		}
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

	private static int getNextId()
	{
		return nextPlaybackId++;
	}
}
