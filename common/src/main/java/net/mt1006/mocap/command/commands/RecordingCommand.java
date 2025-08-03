package net.mt1006.mocap.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.mt1006.mocap.command.CommandSuggestions;
import net.mt1006.mocap.command.CommandUtils;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.recording.RecordingManager;
import net.mt1006.mocap.mocap.recording.RecordingSource;
import net.mt1006.mocap.mocap.settings.Settings;

import java.util.Collection;
import java.util.List;

public class RecordingCommand
{
	private static final Command<CommandSourceStack> COMMAND_START = CommandUtils.command(RecordingCommand::start);

	public static LiteralArgumentBuilder<CommandSourceStack> getArgumentBuilder()
	{
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands.literal("recording");

		commandBuilder.then(Commands.literal("start").executes(COMMAND_START).
			then(Commands.argument("player", EntityArgument.players()).executes(COMMAND_START).
			then(Commands.argument("instant_save", StringArgumentType.string()).executes(COMMAND_START))));
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

	private static boolean start(FullCommandInfo info)
	{
		Collection<ServerPlayer> players;
		String instantSave = null;

		try
		{
			players = EntityArgument.getOptionalPlayers(info.ctx, "player");
			if (players.isEmpty()) { return info.sendFailure("recording.start.player_not_found"); }

			instantSave = info.getNullableString("instant_save");
		}
		catch (Exception e)
		{
			Entity entity = info.getSourceEntity();
			if (!(entity instanceof ServerPlayer))
			{
				// "with_tip" variant contains tip but uses single message to fit in command blocks "previous output" box
				return info.sendFailure(Settings.SHOW_TIPS.val
						? "recording.start.player_not_specified.with_tip"
						: "recording.start.player_not_specified.no_tip");
			}

			players = List.of((ServerPlayer)entity);
		}

		RecordingSource source = RecordingSource.forCommand(info);
		int successes = 0;
		for (ServerPlayer player : players)
		{
			String instantSaveName = (players.size() > 1 && instantSave != null)
					? (instantSave + "_" + player.getName().getString())
					: instantSave;
			successes += (RecordingManager.startOrWait(info, player, source, instantSaveName, players.size() > 1) != null) ? 1 : 0;
		}

		if (players.size() > 1)
		{
			if (successes == players.size()) { info.sendSuccess("recording.start.multiple_started.success"); }
			else if (successes > 0) { info.sendFailure("recording.start.multiple_started.partial_success"); }
			else { info.sendFailure("recording.start.error"); }
		}
		return (successes == players.size());
	}

	private static boolean stop(FullCommandInfo info)
	{
		return RecordingManager.stop(info, info.getNullableString("id"));
	}

	private static boolean discard(FullCommandInfo info)
	{
		return RecordingManager.discard(info, info.getNullableString("id"));
	}

	private static boolean saveAuto(FullCommandInfo info, String name)
	{
		return RecordingManager.save(info, null, name);
	}

	private static boolean saveSpecific(FullCommandInfo info)
	{
		String name = info.getNullableString("name");
		if (name == null) { return info.sendFailure("error.unable_to_get_argument"); }

		return RecordingManager.save(info, info.getNullableString("id"), name);
	}

	private static boolean list(FullCommandInfo info)
	{
		return RecordingManager.list(info, info.getNullableString("id"));
	}
}
