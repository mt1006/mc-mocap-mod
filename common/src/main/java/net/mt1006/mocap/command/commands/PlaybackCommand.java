package net.mt1006.mocap.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.mt1006.mocap.api.impl.modifiers.MocapModifiersImpl;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.controller.playable.MocapPlayable;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.command.CommandSuggestions;
import net.mt1006.mocap.command.CommandUtils;
import net.mt1006.mocap.command.CommandsContext;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.playing.PlaybackManager;
import net.mt1006.mocap.mocap.playing.modifiers.PlaybackModifiers;
import net.mt1006.mocap.mocap.playing.playable.ActiveRecording;
import net.mt1006.mocap.mocap.recording.RecordingContext;
import net.mt1006.mocap.mocap.recording.RecordingManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlaybackCommand
{
	public static LiteralArgumentBuilder<CommandSourceStack> getArgumentBuilder(CommandBuildContext buildContext)
	{
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands.literal("playback");

		commandBuilder.then(Commands.literal("start").
			then(Commands.argument("name", StringArgumentType.string()).
				suggests(CommandSuggestions::playable).executes(CommandUtils.command(PlaybackCommand::start)).
			then(CommandUtils.playerArguments(buildContext, CommandUtils.command(PlaybackCommand::start)))));
		commandBuilder.then(Commands.literal("stop").
			then(Commands.argument("id", StringArgumentType.string()).
				suggests(CommandSuggestions::playbackId).executes(CommandUtils.command(PlaybackCommand::stop))));
		commandBuilder.then(Commands.literal("stop_all").executes(CommandUtils.command((info) -> PlaybackCommand.stopAll(info, false))).
			then(Commands.literal("including_others").executes(CommandUtils.command((info) -> PlaybackCommand.stopAll(info, true)))).
			then(Commands.literal("excluding_others").executes(CommandUtils.command((info) -> PlaybackCommand.stopAll(info, false)))));
		commandBuilder.then(Commands.literal("modifiers").
			then(CommandUtils.withModifiers(buildContext, Commands.literal("set"), CommandUtils.command(PlaybackManager::modifiersSet), false)).
			then(Commands.literal("list").executes(CommandUtils.command(PlaybackManager::modifiersList))).
			then(Commands.literal("reset").executes(CommandUtils.command(PlaybackManager::modifiersReset))).
			then(Commands.literal("add_to").
				then(Commands.argument("scene_name", StringArgumentType.string()).suggests(CommandSuggestions::scene).
				then(Commands.argument("to_add", StringArgumentType.string()).suggests(CommandSuggestions::playable).
					executes(CommandUtils.command(PlaybackCommand::modifiersAddTo))))));
		commandBuilder.then(Commands.literal("list").executes(CommandUtils.command(PlaybackManager::list)));

		return commandBuilder;
	}

	private static boolean start(FullCommandInfo info)
	{
		String name = info.getNullableString("name");
		if (name == null)
		{
			info.sendFailure("error.unable_to_get_argument");
			return false;
		}

		PlaybackModifiers simpleModifiers = info.getSimpleModifiers(info);
		if (simpleModifiers == null) { return false; }

		try
		{
			List<MocapPlayable> playableList = getPlayable(info, name);
			if (playableList.isEmpty()) { return false; }

			PlaybackModifiers finalModifiers = CommandsContext.getFinalModifiers(info.getSourcePlayer(), simpleModifiers);
			MocapModifiers modifiers = MocapModifiersImpl.ofCopy(finalModifiers);

			int successes = 0;
			for (MocapPlayable playable : playableList)
			{
				// each playback should have own copy of config, as it's mutable
				if (playable.startPlayback(info, modifiers, MocapPlaybackConfig.createFromSettings(), false) != null) { successes++; }
			}

			if (successes != 0)
			{
				String key = "playback.start.success";
				if (!CommandsContext.hasDefaultModifiers(info.getSourcePlayer())) { key += ".modifiers"; }

				if (info.getSourcePlayer() != null)
				{
					CommandsContext commandsContext = CommandsContext.get(info.getSourcePlayer());
					if (commandsContext.getSync()) { key += ".sync"; }
				}
				info.sendSuccess(key);
			}
			return successes != 0;
		}
		catch (Exception e) { return info.sendException(e, "playback.start.error"); }
	}

	private static List<MocapPlayable> getPlayable(CommandInfo info, String name)
	{
		if (!name.startsWith("-"))
		{
			MocapPlayable playable = MocapPlayable.get(info, name);
			return playable != null ? List.of(playable) : List.of();
		}
		else
		{
			Collection<RecordingContext> contexts = RecordingManager.resolveContexts(info, name);
			if (contexts == null) { return List.of(); }

			List<MocapPlayable> list = new ArrayList<>();
			contexts.forEach((ctx) -> list.add(ActiveRecording.get(ctx)));
			return list;
		}
	}

	private static boolean stop(FullCommandInfo info)
	{
		try
		{
			Pair<String, String> idPair = CommandUtils.splitIdStr(info.getString("id"));
			return PlaybackManager.stop(info, idPair.getFirst(), idPair.getSecond());
		}
		catch (IllegalArgumentException e) { return info.sendException(e, "error.unable_to_get_argument"); }
	}

	private static boolean stopAll(CommandInfo info, boolean includeOthers)
	{
		PlaybackManager.stopAll(info, includeOthers ? null : info.getSourcePlayer());
		return true;
	}

	private static boolean modifiersAddTo(FullCommandInfo info)
	{
		try
		{
			String name = info.getString("scene_name");
			String toAdd = info.getString("to_add");
			return PlaybackManager.modifiersAddTo(info, name, toAdd);
		}
		catch (IllegalArgumentException e)
		{
			info.sendException(e, "error.unable_to_get_argument");
			return false;
		}
	}
}
