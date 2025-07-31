package net.mt1006.mocap.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.command.CommandSuggestions;
import net.mt1006.mocap.command.CommandUtils;
import net.mt1006.mocap.command.CommandsContext;
import net.mt1006.mocap.command.io.CommandInfo;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.playing.Playing;
import net.mt1006.mocap.mocap.playing.modifiers.PlaybackModifiers;

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
			then(CommandUtils.withModifiers(buildContext, Commands.literal("set"), CommandUtils.command(Playing::modifiersSet), false)).
			then(Commands.literal("list").executes(CommandUtils.command(Playing::modifiersList))).
			then(Commands.literal("reset").executes(CommandUtils.command(Playing::modifiersReset))).
			then(Commands.literal("add_to").
				then(Commands.argument("scene_name", StringArgumentType.string()).suggests(CommandSuggestions::scene).
				then(Commands.argument("to_add", StringArgumentType.string()).suggests(CommandSuggestions::playable).
					executes(CommandUtils.command(PlaybackCommand::modifiersAddTo))))));
		commandBuilder.then(Commands.literal("list").executes(CommandUtils.command(Playing::list)));

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

		PlaybackModifiers modifiers = info.getSimpleModifiers(info);
		if (modifiers == null) { return false; }

		try
		{
			PlaybackModifiers finalModifiers = CommandsContext.getFinalModifiers(info.getSourcePlayer(), modifiers);
			boolean sendModifiersWarning = !CommandsContext.hasDefaultModifiers(info.getSourcePlayer());
			return Playing.start(info, name, MocapPlaybackConfig.createFromSettings(), finalModifiers, sendModifiersWarning);
		}
		catch (Exception e) { return info.sendException(e, "playback.start.error"); }
	}

	private static boolean stop(FullCommandInfo info)
	{
		try
		{
			Pair<String, String> idPair = CommandUtils.splitIdStr(info.getString("id"));
			return Playing.stop(info, idPair.getFirst(), idPair.getSecond());
		}
		catch (IllegalArgumentException e) { return info.sendException(e, "error.unable_to_get_argument"); }
	}

	private static boolean stopAll(CommandInfo info, boolean includeOthers)
	{
		Playing.stopAll(info, includeOthers ? null : info.getSourcePlayer());
		return true;
	}

	private static boolean modifiersAddTo(FullCommandInfo info)
	{
		try
		{
			String name = info.getString("scene_name");
			String toAdd = info.getString("to_add");
			return Playing.modifiersAddTo(info, name, toAdd);
		}
		catch (IllegalArgumentException e)
		{
			info.sendException(e, "error.unable_to_get_argument");
			return false;
		}
	}
}
