package com.mt1006.mocap.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mt1006.mocap.command.CommandInfo;
import com.mt1006.mocap.command.CommandUtils;
import com.mt1006.mocap.mocap.playing.PlayerData;
import com.mt1006.mocap.mocap.playing.Playing;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class PlayingCommand
{
	public static LiteralArgumentBuilder<CommandSource> getArgumentBuilder()
	{
		LiteralArgumentBuilder<CommandSource> commandBuilder = Commands.literal("playing");

		commandBuilder.then(Commands.literal("start").
			then(Commands.argument("name", StringArgumentType.string()).executes(CommandUtils.command(PlayingCommand::start)).
			then(CommandUtils.withPlayerArguments(CommandUtils.command(PlayingCommand::start)))));
		commandBuilder.then(Commands.literal("stop").
			then(Commands.argument("id", IntegerArgumentType.integer()).executes(CommandUtils.command(PlayingCommand::stop))));
		commandBuilder.then(Commands.literal("stopAll").executes(CommandUtils.command(Playing::stopAll)));
		commandBuilder.then(Commands.literal("list").executes(CommandUtils.command(Playing::list)));

		return commandBuilder;
	}

	private static boolean start(CommandInfo commandInfo)
	{
		String name = commandInfo.getNullableString("name");
		PlayerData playerData = commandInfo.getPlayerData();

		if (name == null)
		{
			commandInfo.sendFailure("mocap.error.unable_to_get_argument");
			return false;
		}

		try
		{
			return Playing.start(commandInfo, name, playerData);
		}
		catch (Exception exception)
		{
			commandInfo.sendException(exception, "mocap.playing.start.error");
			return false;
		}
	}

	private static boolean stop(CommandInfo commandInfo)
	{
		try
		{
			int id = commandInfo.getInteger("id");
			Playing.stop(commandInfo, id);
		}
		catch (Exception exception)
		{
			commandInfo.sendException(exception, "mocap.error.unable_to_get_argument");
			return false;
		}
		return true;
	}
}
