package com.mt1006.mocap.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mt1006.mocap.command.CommandInfo;
import com.mt1006.mocap.command.CommandUtils;
import com.mt1006.mocap.mocap.files.SceneFiles;
import com.mt1006.mocap.mocap.playing.SceneData;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntitySummonArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;

import java.util.List;

public class ScenesCommand
{
	private static final Command<CommandSourceStack> COMMAND_ADD_TO = CommandUtils.command(ScenesCommand::addTo);
	private static final Command<CommandSourceStack> COMMAND_MODIFY = CommandUtils.command(ScenesCommand::modify);

	public static LiteralArgumentBuilder<CommandSourceStack> getArgumentBuilder()
	{
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands.literal("scenes");

		commandBuilder.then(Commands.literal("add").then(CommandUtils.withStringArgument(SceneFiles::add, "name")));
		commandBuilder.then(Commands.literal("copy").then(CommandUtils.withTwoStringArguments(SceneFiles::copy, "srcName", "destName")));
		commandBuilder.then(Commands.literal("rename").then(CommandUtils.withTwoStringArguments(SceneFiles::rename, "oldName", "newName")));
		commandBuilder.then(Commands.literal("remove").then(CommandUtils.withStringArgument(SceneFiles::remove, "name")));
		commandBuilder.then(Commands.literal("addTo").
			then(Commands.argument("sceneName", StringArgumentType.string()).
			then(Commands.argument("toAdd", StringArgumentType.string()).executes(COMMAND_ADD_TO).
			then(Commands.argument("startDelay", DoubleArgumentType.doubleArg(0.0)).executes(COMMAND_ADD_TO).
			then(Commands.argument("offsetX", DoubleArgumentType.doubleArg()).executes(COMMAND_ADD_TO).
			then(Commands.argument("offsetY", DoubleArgumentType.doubleArg()).executes(COMMAND_ADD_TO).
			then(Commands.argument("offsetZ", DoubleArgumentType.doubleArg()).executes(COMMAND_ADD_TO).
			then(CommandUtils.withPlayerArguments(COMMAND_ADD_TO)))))))));
		commandBuilder.then(Commands.literal("removeFrom").
			then(Commands.argument("sceneName", StringArgumentType.string()).
			then(Commands.argument("toRemove", IntegerArgumentType.integer()).executes(CommandUtils.command(ScenesCommand::removeFrom)))));
		commandBuilder.then(Commands.literal("modify").
			then(Commands.argument("sceneName", StringArgumentType.string()).
			then(Commands.argument("toModify", IntegerArgumentType.integer()).
			then(Commands.literal("subsceneName").then(Commands.argument("newName", StringArgumentType.string()).executes(COMMAND_MODIFY))).
			then(Commands.literal("startDelay").then(Commands.argument("delay", DoubleArgumentType.doubleArg(0.0)).executes(COMMAND_MODIFY))).
			then(Commands.literal("positionOffset").
				then(Commands.argument("offsetX", DoubleArgumentType.doubleArg()).
				then(Commands.argument("offsetY", DoubleArgumentType.doubleArg()).
				then(Commands.argument("offsetZ", DoubleArgumentType.doubleArg()).executes(COMMAND_MODIFY))))).
			then(Commands.literal("playerInfo").then(CommandUtils.withPlayerArguments(COMMAND_MODIFY))).
			then(Commands.literal("playerAsEntity").
				then(Commands.literal("disabled").executes(COMMAND_MODIFY)).
				then(Commands.literal("enabled").
					then(Commands.argument("entity", EntitySummonArgument.id()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).
						executes(COMMAND_MODIFY)))))));
		commandBuilder.then(Commands.literal("elementInfo").
			then(Commands.argument("sceneName", StringArgumentType.string()).
			then(Commands.argument("elementPos", IntegerArgumentType.integer()).executes(CommandUtils.command(ScenesCommand::elementInfo)))));
		commandBuilder.then(Commands.literal("listElements").then(CommandUtils.withStringArgument(SceneFiles::listElements, "name")));
		commandBuilder.then(Commands.literal("info").then(CommandUtils.withStringArgument(SceneFiles::info, "name")));
		commandBuilder.then(Commands.literal("list").executes(CommandUtils.command(ScenesCommand::list)));

		return commandBuilder;
	}

	private static boolean addTo(CommandInfo commandInfo)
	{
		try
		{
			String name = commandInfo.getString("sceneName");

			String toAdd = commandInfo.getString("toAdd");
			SceneData.Subscene subscene = new SceneData.Subscene(toAdd);

			try
			{
				subscene.startDelay = commandInfo.getDouble("startDelay");
				subscene.posOffset[0] = commandInfo.getDouble("offsetX");
				subscene.posOffset[1] = commandInfo.getDouble("offsetY");
				subscene.posOffset[2] = commandInfo.getDouble("offsetZ");
				subscene.playerData = commandInfo.getPlayerData();

				if (subscene.playerData.name != null)
				{
					//TODO: move
					if (subscene.playerData.name.length() > 16)
					{
						commandInfo.sendFailure("mocap.scenes.add_to.error");
						commandInfo.sendFailure("mocap.scenes.add_to.error.too_long_name");
						return false;
					}

					if (subscene.playerData.name.contains(" "))
					{
						commandInfo.sendFailure("mocap.scenes.add_to.error");
						commandInfo.sendFailure("mocap.scenes.add_to.error.contain_spaces");
						return false;
					}
				}
			}
			catch (Exception ignore) {}

			return SceneFiles.addElement(commandInfo, name, subscene.sceneToStr());
		}
		catch (Exception exception)
		{
			commandInfo.sendException(exception, "mocap.error.unable_to_get_argument");
			return false;
		}
	}

	private static boolean removeFrom(CommandInfo commandInfo)
	{
		try
		{
			String name = commandInfo.getString("sceneName");
			int pos = commandInfo.getInteger("toRemove");

			return SceneFiles.removeElement(commandInfo, name, pos);
		}
		catch (Exception exception)
		{
			commandInfo.sendException(exception, "mocap.error.unable_to_get_argument");
			return false;
		}
	}

	private static boolean modify(CommandInfo commandInfo)
	{
		try
		{
			String name = commandInfo.getString("sceneName");
			int pos = commandInfo.getInteger("toModify");

			return SceneFiles.modify(commandInfo, name, pos);
		}
		catch (Exception exception)
		{
			commandInfo.sendException(exception, "mocap.error.unable_to_get_argument");
			return false;
		}
	}

	private static boolean elementInfo(CommandInfo commandInfo)
	{
		try
		{
			String name = commandInfo.getString("sceneName");
			int pos = commandInfo.getInteger("elementPos");

			return SceneFiles.elementInfo(commandInfo, name, pos);
		}
		catch (Exception exception)
		{
			commandInfo.sendException(exception, "mocap.error.unable_to_get_argument");
			return false;
		}
	}

	public static boolean list(CommandInfo commandInfo)
	{
		StringBuilder scenesListStr = new StringBuilder();
		List<String> scenesList = SceneFiles.list(commandInfo.source.getServer(), commandInfo);

		if (scenesList == null)
		{
			scenesListStr.append(" ").append(Utils.stringFromComponent("mocap.list.error"));
		}
		else if (!scenesList.isEmpty())
		{
			for (String name : scenesList)
			{
				scenesListStr.append(" ").append(name);
			}
		}
		else
		{
			scenesListStr.append(" ").append(Utils.stringFromComponent("mocap.list.empty"));
		}

		commandInfo.sendSuccess("mocap.scenes.list", new String(scenesListStr));
		return true;
	}
}
