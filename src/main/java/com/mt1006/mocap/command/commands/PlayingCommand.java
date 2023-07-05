package com.mt1006.mocap.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mt1006.mocap.command.CommandUtils;
import com.mt1006.mocap.mocap.playing.PlayerData;
import com.mt1006.mocap.mocap.playing.Playing;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class PlayingCommand
{
	public static LiteralArgumentBuilder<CommandSource> getArgumentBuilder()
	{
		LiteralArgumentBuilder<CommandSource> commandBuilder = Commands.literal("playing");

		commandBuilder.then(Commands.literal("start").
			then(Commands.argument("name", StringArgumentType.string()).executes(PlayingCommand::start).
			then(CommandUtils.withPlayerArguments(PlayingCommand::start))));
		commandBuilder.then(Commands.literal("stop").
			then(Commands.argument("id", IntegerArgumentType.integer()).executes(PlayingCommand::stop)));
		commandBuilder.then(Commands.literal("stopAll").executes(CommandUtils.simpleCommand(Playing::stopAll)));
		commandBuilder.then(Commands.literal("list").executes(CommandUtils.simpleCommand(Playing::list)));

		return commandBuilder;
	}

	private static int start(CommandContext<CommandSource> ctx)
	{
		String name = CommandUtils.getString(ctx, "name");
		PlayerData playerData = CommandUtils.getPlayerData(ctx);

		if (name == null)
		{
			Utils.sendFailure(ctx.getSource(), "mocap.error.unable_to_get_argument");
			return 0;
		}

		try
		{
			return Playing.start(ctx.getSource(), name, playerData);
		}
		catch (Exception exception)
		{
			Utils.sendException(exception, ctx.getSource(), "mocap.playing.start.error");
			return 0;
		}
	}

	private static int stop(CommandContext<CommandSource> ctx)
	{
		try
		{
			int id = IntegerArgumentType.getInteger(ctx, "id");
			Playing.stop(ctx.getSource(), id);
		}
		catch (Exception exception)
		{
			Utils.sendException(exception, ctx.getSource(), "mocap.error.unable_to_get_argument");
			return 0;
		}
		return 1;
	}
}
