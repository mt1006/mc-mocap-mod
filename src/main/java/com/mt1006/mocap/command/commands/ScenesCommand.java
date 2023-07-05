package com.mt1006.mocap.command.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
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
	public static LiteralArgumentBuilder<CommandSourceStack> getArgumentBuilder()
	{
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands.literal("scenes");

		commandBuilder.then(Commands.literal("add").then(CommandUtils.withStringArgument(SceneFiles::add, "name")));
		commandBuilder.then(Commands.literal("copy").then(CommandUtils.withTwoStringArguments(SceneFiles::copy, "srcName", "destName")));
		commandBuilder.then(Commands.literal("rename").then(CommandUtils.withTwoStringArguments(SceneFiles::rename, "oldName", "newName")));
		commandBuilder.then(Commands.literal("remove").then(CommandUtils.withStringArgument(SceneFiles::remove, "name")));
		commandBuilder.then(Commands.literal("addTo").
			then(Commands.argument("sceneName", StringArgumentType.string()).
			then(Commands.argument("toAdd", StringArgumentType.string()).executes(ScenesCommand::addTo).
			then(Commands.argument("startDelay", DoubleArgumentType.doubleArg(0.0)).executes(ScenesCommand::addTo).
			then(Commands.argument("offsetX", DoubleArgumentType.doubleArg()).executes(ScenesCommand::addTo).
			then(Commands.argument("offsetY", DoubleArgumentType.doubleArg()).executes(ScenesCommand::addTo).
			then(Commands.argument("offsetZ", DoubleArgumentType.doubleArg()).executes(ScenesCommand::addTo).
			then(CommandUtils.withPlayerArguments(ScenesCommand::addTo)))))))));
		commandBuilder.then(Commands.literal("removeFrom").
			then(Commands.argument("sceneName", StringArgumentType.string()).
			then(Commands.argument("toRemove", IntegerArgumentType.integer()).executes(ScenesCommand::removeFrom))));
		commandBuilder.then(Commands.literal("modify").
			then(Commands.argument("sceneName", StringArgumentType.string()).
			then(Commands.argument("toModify", IntegerArgumentType.integer()).
			then(Commands.literal("subsceneName").then(Commands.argument("newName", StringArgumentType.string()).executes(ScenesCommand::modify))).
			then(Commands.literal("startDelay").then(Commands.argument("delay", DoubleArgumentType.doubleArg(0.0)).executes(ScenesCommand::modify))).
			then(Commands.literal("positionOffset").
				then(Commands.argument("offsetX", DoubleArgumentType.doubleArg()).
				then(Commands.argument("offsetY", DoubleArgumentType.doubleArg()).
				then(Commands.argument("offsetZ", DoubleArgumentType.doubleArg()).executes(ScenesCommand::modify))))).
			then(Commands.literal("playerInfo").then(CommandUtils.withPlayerArguments(ScenesCommand::modify))).
			then(Commands.literal("playerAsEntity").
				then(Commands.literal("disabled").executes(ScenesCommand::modify)).
				then(Commands.literal("enabled").
					then(Commands.argument("entity", EntitySummonArgument.id()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).
						executes(ScenesCommand::modify)))))));
		commandBuilder.then(Commands.literal("elementInfo").
			then(Commands.argument("sceneName", StringArgumentType.string()).
			then(Commands.argument("elementPos", IntegerArgumentType.integer()).executes(ScenesCommand::elementInfo))));
		commandBuilder.then(Commands.literal("listElements").then(CommandUtils.withStringArgument(SceneFiles::listElements, "name")));
		commandBuilder.then(Commands.literal("info").then(CommandUtils.withStringArgument(SceneFiles::info, "name"))); //TODO: check
		commandBuilder.then(Commands.literal("list").executes(CommandUtils.simpleCommand(ScenesCommand::list)));

		return commandBuilder;
	}

	private static int addTo(CommandContext<CommandSourceStack> ctx)
	{
		try
		{
			String name = StringArgumentType.getString(ctx, "sceneName");

			String toAdd = StringArgumentType.getString(ctx, "toAdd");
			SceneData.Subscene subscene = new SceneData.Subscene(toAdd);

			try
			{
				subscene.startDelay = DoubleArgumentType.getDouble(ctx, "startDelay");
				subscene.posOffset[0] = DoubleArgumentType.getDouble(ctx, "offsetX");
				subscene.posOffset[1] = DoubleArgumentType.getDouble(ctx, "offsetY");
				subscene.posOffset[2] = DoubleArgumentType.getDouble(ctx, "offsetZ");
				subscene.playerData = CommandUtils.getPlayerData(ctx);

				if (subscene.playerData.name != null)
				{
					//TODO: move
					if (subscene.playerData.name.length() > 16)
					{
						Utils.sendFailure(ctx.getSource(), "mocap.scenes.add_to.error");
						Utils.sendFailure(ctx.getSource(), "mocap.scenes.add_to.error.too_long_name");
						return 0;
					}

					if (subscene.playerData.name.contains(" "))
					{
						Utils.sendFailure(ctx.getSource(), "mocap.scenes.add_to.error");
						Utils.sendFailure(ctx.getSource(), "mocap.scenes.add_to.error.contain_spaces");
						return 0;
					}
				}
			}
			catch (Exception ignore) {}

			return SceneFiles.addElement(ctx.getSource(), name, subscene.sceneToStr()) ? 1 : 0;
		}
		catch (Exception exception)
		{
			Utils.sendException(exception, ctx.getSource(), "mocap.error.unable_to_get_argument");
			return 0;
		}
	}

	private static int removeFrom(CommandContext<CommandSourceStack> ctx)
	{
		try
		{
			String name = StringArgumentType.getString(ctx, "sceneName");
			int pos = IntegerArgumentType.getInteger(ctx, "toRemove");

			return SceneFiles.removeElement(ctx.getSource(), name, pos) ? 1 : 0;
		}
		catch (Exception exception)
		{
			Utils.sendException(exception, ctx.getSource(), "mocap.error.unable_to_get_argument");
			return 0;
		}
	}

	private static int modify(CommandContext<CommandSourceStack> ctx)
	{
		try
		{
			String name = StringArgumentType.getString(ctx, "sceneName");
			int pos = IntegerArgumentType.getInteger(ctx, "toModify");

			return SceneFiles.modify(ctx, name, pos) ? 1 : 0;
		}
		catch (Exception exception)
		{
			Utils.sendException(exception, ctx.getSource(), "mocap.error.unable_to_get_argument");
			return 0;
		}
	}

	private static int elementInfo(CommandContext<CommandSourceStack> ctx)
	{
		try
		{
			String name = StringArgumentType.getString(ctx, "sceneName");
			int pos = IntegerArgumentType.getInteger(ctx, "elementPos");

			return SceneFiles.elementInfo(ctx.getSource(), name, pos) ? 1 : 0;
		}
		catch (Exception exception)
		{
			Utils.sendException(exception, ctx.getSource(), "mocap.error.unable_to_get_argument");
			return 0;
		}
	}

	public static boolean list(CommandSourceStack commandSource)
	{
		StringBuilder scenesListStr = new StringBuilder();
		List<String> scenesList = SceneFiles.list(commandSource.getServer(), commandSource);

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

		Utils.sendSuccess(commandSource, "mocap.scenes.list", new String(scenesListStr));
		return true;
	}
}
