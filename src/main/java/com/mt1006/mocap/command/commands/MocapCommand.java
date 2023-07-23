package com.mt1006.mocap.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.command.CommandInfo;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class MocapCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		//TODO: add permission level to config
		LiteralArgumentBuilder<CommandSource> commandBuilder = Commands.literal("mocap").requires(source -> source.hasPermission(2));

		commandBuilder.then(RecordingsCommand.getArgumentBuilder());
		commandBuilder.then(ScenesCommand.getArgumentBuilder());
		commandBuilder.then(RecordingCommand.getArgumentBuilder());
		commandBuilder.then(PlayingCommand.getArgumentBuilder());
		commandBuilder.then(SettingsCommand.getArgumentBuilder());
		commandBuilder.then(Commands.literal("info").executes(MocapCommand::info));
		commandBuilder.then(Commands.literal("help").executes(MocapCommand::help));

		dispatcher.register(commandBuilder);
	}

	private static int info(CommandContext<CommandSource> ctx)
	{
		CommandInfo commandInfo = new CommandInfo(ctx);
		commandInfo.sendSuccessLiteral(MocapMod.getFullName());
		commandInfo.sendSuccessLiteral("Author: mt1006 (mt1006x)");
		return 1;
	}

	private static int help(CommandContext<CommandSource> ctx)
	{
		new CommandInfo(ctx).sendSuccess("mocap.help", MocapMod.getName());
		return 1;
	}
}
