package com.mt1006.mocap.command.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mt1006.mocap.command.CommandUtils;
import com.mt1006.mocap.mocap.recording.Recording;
import com.mt1006.mocap.utils.Utils;
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

		commandBuilder.then(Commands.literal("start").executes(RecordingCommand::start).
			then(Commands.argument("player", GameProfileArgument.gameProfile()).executes(RecordingCommand::start)));
		commandBuilder.then(Commands.literal("stop").executes(RecordingCommand::stop));
		commandBuilder.then(Commands.literal("save").then(CommandUtils.withStringArgument(Recording::save, "name")));
		commandBuilder.then(Commands.literal("state").executes(RecordingCommand::state));

		return commandBuilder;
	}

	private static int start(CommandContext<CommandSourceStack> ctx)
	{
		ServerPlayer serverPlayer;

		try
		{
			Collection<GameProfile> gameProfiles = GameProfileArgument.getGameProfiles(ctx, "player");

			if (gameProfiles.size() != 1)
			{
				Utils.sendFailure(ctx.getSource(), "mocap.recording.start.player_not_found");
				return 0;
			}

			String nickname = gameProfiles.iterator().next().getName();
			serverPlayer = ctx.getSource().getServer().getPlayerList().getPlayerByName(nickname);

			if(serverPlayer == null)
			{
				Utils.sendFailure(ctx.getSource(), "mocap.recording.start.player_not_found");
				return 0;
			}
		}
		catch (Exception exception)
		{
			Entity entity = ctx.getSource().getEntity();

			if (!(entity instanceof ServerPlayer))
			{
				Utils.sendFailure(ctx.getSource(), "mocap.recording.start.player_not_specified");
				Utils.sendFailure(ctx.getSource(), "mocap.recording.start.player_not_specified.tip");
				return 0;
			}

			serverPlayer = (ServerPlayer)entity;
		}

		return Recording.start(ctx.getSource(), serverPlayer) ? 1 : 0;
	}

	//TODO: don't replace - extend it
	private static int stop(CommandContext<CommandSourceStack> ctx)
	{
		return Recording.stop(ctx.getSource()) ? 1 : 0;
	}

	private static int state(CommandContext<CommandSourceStack> ctx)
	{
		return Recording.state(ctx.getSource()) ? 1 : 0;
	}
}
