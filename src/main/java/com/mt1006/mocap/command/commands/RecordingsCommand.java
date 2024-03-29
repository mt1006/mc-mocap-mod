package com.mt1006.mocap.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mt1006.mocap.command.CommandInfo;
import com.mt1006.mocap.command.CommandUtils;
import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.ArrayList;

public class RecordingsCommand
{
	public static LiteralArgumentBuilder<CommandSourceStack> getArgumentBuilder()
	{
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands.literal("recordings");

		commandBuilder.then(Commands.literal("copy").then(CommandUtils.withTwoStringArguments(RecordingFiles::copy, "srcName", "destName")));
		commandBuilder.then(Commands.literal("rename").then(CommandUtils.withTwoStringArguments(RecordingFiles::rename, "oldName", "newName")));
		commandBuilder.then(Commands.literal("remove").then(CommandUtils.withStringArgument(RecordingFiles::remove, "name")));
		commandBuilder.then(Commands.literal("info").then(CommandUtils.withStringArgument(RecordingFiles::info, "name")));
		commandBuilder.then(Commands.literal("list").executes(CommandUtils.command(RecordingsCommand::list)));

		return commandBuilder;
	}

	public static boolean list(CommandInfo commandInfo)
	{
		StringBuilder recordingsListStr = new StringBuilder();
		ArrayList<String> recordingsList = RecordingFiles.list(commandInfo.source.getServer(), commandInfo);

		if (recordingsList == null)
		{
			recordingsListStr.append(" ").append(Utils.stringFromComponent("mocap.list.error"));
		}
		else if (!recordingsList.isEmpty())
		{
			for (String name : recordingsList)
			{
				recordingsListStr.append(" ").append(name);
			}
		}
		else
		{
			recordingsListStr.append(" ").append(Utils.stringFromComponent("mocap.list.empty"));
		}

		commandInfo.sendSuccess("mocap.recordings.list", new String(recordingsListStr));
		return true;
	}
}
