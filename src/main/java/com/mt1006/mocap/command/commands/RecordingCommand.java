package com.mt1006.mocap.command.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mt1006.mocap.command.CommandInfo;
import com.mt1006.mocap.command.CommandUtils;
import com.mt1006.mocap.mocap.recording.Recording;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Collection;

public class RecordingCommand
{
	public static LiteralArgumentBuilder<CommandSourceStack> getArgumentBuilder()
	{
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands.literal("recording");

		commandBuilder.then(Commands.literal("start").executes(CommandUtils.command(RecordingCommand::start)).
			then(Commands.argument("player", GameProfileArgument.gameProfile()).executes(CommandUtils.command(RecordingCommand::start))));
		commandBuilder.then(Commands.literal("stop").executes(CommandUtils.command(RecordingCommand::stop)));
		commandBuilder.then(Commands.literal("save").then(CommandUtils.withStringArgument(Recording::save, "name")));
		commandBuilder.then(Commands.literal("state").executes(CommandUtils.command(RecordingCommand::state)));

		return commandBuilder;
	}

	private static boolean start(CommandInfo commandInfo)
	{
		ServerPlayer serverPlayer = null;

		try
		{
			Collection<GameProfile> gameProfiles = commandInfo.getGameProfiles("player");

			if (gameProfiles.size() == 1)
			{
				String nickname = gameProfiles.iterator().next().getName();
				serverPlayer = commandInfo.source.getServer().getPlayerList().getPlayerByName(nickname);
			}

			if (serverPlayer == null)
			{
				commandInfo.sendFailure("mocap.recording.start.player_not_found");
				return false;
			}
		}
		catch (Exception exception)
		{
			Entity entity = commandInfo.source.getEntity();

			if (!(entity instanceof ServerPlayer))
			{
				commandInfo.sendFailure("mocap.recording.start.player_not_specified");
				commandInfo.sendFailure("mocap.recording.start.player_not_specified.tip");
				return false;
			}

			serverPlayer = (ServerPlayer)entity;
		}

		return Recording.start(commandInfo, serverPlayer);
	}

	//TODO: don't replace - extend it
	private static boolean stop(CommandInfo commandInfo)
	{
		return Recording.stop(commandInfo);
	}

	private static boolean state(CommandInfo commandInfo)
	{
		return Recording.state(commandInfo);
	}
}
