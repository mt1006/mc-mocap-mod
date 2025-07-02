package net.mt1006.mocap.command.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.mt1006.mocap.command.CommandSuggestions;
import net.mt1006.mocap.command.CommandUtils;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.recording.Recording;
import net.mt1006.mocap.mocap.recording.RecordingSource;
import net.mt1006.mocap.mocap.settings.Settings;

import java.util.Collection;

public class RecordingCommand
{
	private static final Command<CommandSourceStack> COMMAND_START = CommandUtils.command(RecordingCommand::start);

	public static LiteralArgumentBuilder<CommandSourceStack> getArgumentBuilder()
	{
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands.literal("recording");

		commandBuilder.then(Commands.literal("start").executes(COMMAND_START).
			then(Commands.argument("player", GameProfileArgument.gameProfile()).executes(COMMAND_START).
			then(Commands.argument("instant_save", StringArgumentType.string()).executes(COMMAND_START))));
		//commandBuilder.then(Commands.literal("start_multiple").then(CommandUtils.withStringArgument(Recording::startMultiple, "players"))); //TODO: todo?
		commandBuilder.then(Commands.literal("stop").executes(CommandUtils.command(RecordingCommand::stop)).
			then(Commands.argument("id", StringArgumentType.string()).suggests(CommandSuggestions::currentlyRecorded).executes(CommandUtils.command(RecordingCommand::stop))));
		commandBuilder.then(Commands.literal("discard").executes(CommandUtils.command(RecordingCommand::discard)).
			then(Commands.argument("id", StringArgumentType.string()).suggests(CommandSuggestions::currentlyRecorded).executes(CommandUtils.command(RecordingCommand::discard))));
		commandBuilder.then(Commands.literal("save").then(CommandUtils.withStringArgument(RecordingCommand::saveAuto, "name").
			then(Commands.argument("id", StringArgumentType.string()).suggests(CommandSuggestions::currentlyRecorded).executes(CommandUtils.command(RecordingCommand::saveSpecific)))));
		commandBuilder.then(Commands.literal("list").executes(CommandUtils.command(RecordingCommand::list)).
			then(Commands.argument("id", StringArgumentType.string()).suggests(CommandSuggestions::currentlyRecorded).executes(CommandUtils.command(RecordingCommand::list))));

		return commandBuilder;
	}

	private static boolean start(FullCommandInfo commandInfo)
	{
		ServerPlayer player = null;
		String instantSave = null;

		try
		{
			Collection<GameProfile> gameProfiles = commandInfo.getGameProfiles("player");

			if (gameProfiles.size() == 1)
			{
				String nickname = gameProfiles.iterator().next().getName();
				player = commandInfo.getServer().getPlayerList().getPlayerByName(nickname);
			}
			if (player == null)
			{
				commandInfo.sendFailure("recording.start.player_not_found");
				return false;
			}

			instantSave = commandInfo.getNullableString("instant_save");
		}
		catch (Exception e)
		{
			Entity entity = commandInfo.getSourceEntity();
			if (!(entity instanceof ServerPlayer))
			{
				// "with_tip" variant contains tip but uses single message to fit in command blocks "previous output" box
				commandInfo.sendFailure(Settings.SHOW_TIPS.val
						? "recording.start.player_not_specified.with_tip"
						: "recording.start.player_not_specified.no_tip");
				return false;
			}

			player = (ServerPlayer)entity;
		}

		return (Recording.startOrWait(commandInfo, player, RecordingSource.forCommand(commandInfo), instantSave) != null);
	}

	private static boolean stop(FullCommandInfo commandInfo)
	{
		return Recording.stop(commandInfo, commandInfo.getNullableString("id"));
	}

	private static boolean discard(FullCommandInfo commandInfo)
	{
		return Recording.discard(commandInfo, commandInfo.getNullableString("id"));
	}

	private static boolean saveAuto(FullCommandInfo commandInfo, String name)
	{
		return Recording.save(commandInfo, null, name);
	}

	private static boolean saveSpecific(FullCommandInfo commandInfo)
	{
		String name = commandInfo.getNullableString("name");
		if (name == null)
		{
			commandInfo.sendFailure("error.unable_to_get_argument");
			return false;
		}

		return Recording.save(commandInfo, commandInfo.getNullableString("id"), name);
	}

	private static boolean list(FullCommandInfo commandInfo)
	{
		return Recording.list(commandInfo, commandInfo.getNullableString("id"));
	}
}
