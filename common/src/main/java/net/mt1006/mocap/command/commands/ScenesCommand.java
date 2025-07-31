package net.mt1006.mocap.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.mt1006.mocap.command.CommandSuggestions;
import net.mt1006.mocap.command.CommandUtils;
import net.mt1006.mocap.command.io.CommandOutput;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.files.RecordingFiles;
import net.mt1006.mocap.mocap.files.SceneData;
import net.mt1006.mocap.mocap.files.SceneFiles;
import net.mt1006.mocap.mocap.playing.modifiers.PlaybackModifiers;
import net.mt1006.mocap.mocap.playing.modifiers.StartDelay;
import net.mt1006.mocap.mocap.recording.Recording;
import net.mt1006.mocap.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ScenesCommand
{
	private static final Command<CommandSourceStack> COMMAND_ADD_TO = CommandUtils.command(ScenesCommand::addTo);

	public static LiteralArgumentBuilder<CommandSourceStack> getArgumentBuilder(CommandBuildContext buildContext)
	{
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands.literal("scenes");

		commandBuilder.then(Commands.literal("add").then(CommandUtils.withStringArgument(SceneFiles::add, "name")));
		commandBuilder.then(Commands.literal("copy").then(CommandUtils.withInputAndStringArgument(SceneFiles::copy, CommandSuggestions::scene, "src_name", "dest_name")));
		commandBuilder.then(Commands.literal("rename").then(CommandUtils.withInputAndStringArgument(SceneFiles::rename, CommandSuggestions::scene, "old_name", "new_name")));
		commandBuilder.then(Commands.literal("remove").then(CommandUtils.withInputArgument(SceneFiles::remove, CommandSuggestions::scene, "name")));
		commandBuilder.then(Commands.literal("add_to").
			then(Commands.argument("scene_name", StringArgumentType.string()).suggests(CommandSuggestions::scene).
			then(Commands.argument("to_add", StringArgumentType.string()).suggests(CommandSuggestions::playable).executes(CommandUtils.command(ScenesCommand::addToMinimal)).
			then(Commands.argument("start_delay", DoubleArgumentType.doubleArg(0.0)).executes(COMMAND_ADD_TO).
			then(CommandUtils.playerArguments(buildContext, COMMAND_ADD_TO))))));
		commandBuilder.then(Commands.literal("remove_from").
			then(CommandUtils.withTwoInputArguments(SceneFiles::removeElement, CommandSuggestions::scene, CommandSuggestions::sceneElement, "scene_name", "to_remove")));
		commandBuilder.then(Commands.literal("modify").
			then(Commands.argument("scene_name", StringArgumentType.string()).suggests(CommandSuggestions::scene).
			then(CommandUtils.withModifiers(buildContext, Commands.argument("to_modify", StringArgumentType.string()).
				suggests(CommandSuggestions::sceneElement), CommandUtils.command(ScenesCommand::modify), true))));
		commandBuilder.then(Commands.literal("info").then(CommandUtils.withStringArgument(SceneFiles::info, "scene_name").suggests(CommandSuggestions::scene)).
			then(CommandUtils.withTwoInputArguments(SceneFiles::elementInfo, CommandSuggestions::scene, CommandSuggestions::sceneElement, "scene_name", "element_pos")));
		commandBuilder.then(Commands.literal("list").executes(CommandUtils.command(ScenesCommand::list)).
			then(CommandUtils.withInputArgument(SceneFiles::listElements, CommandSuggestions::scene, "scene_name")));

		return commandBuilder;
	}

	private static boolean addToMinimal(FullCommandInfo info)
	{
		// separated from addTo because it supports name pattern (adding multiple elements with single command)
		try
		{
			String name = info.getString("scene_name");
			String toAdd = info.getString("to_add");

			if (!toAdd.contains("*"))
			{
				SceneData.Subscene subscene = new SceneData.Subscene(toAdd, PlaybackModifiers.empty());
				return SceneFiles.addElement(info, name, subscene);
			}
			else
			{
				String[] parts = toAdd.split("\\*", -1);
				if (parts.length != 2)
				{
					info.sendFailure("scenes.add_to.multiple.invalid_pattern");
					return false;
				}

				//TODO: rely on file list
				List<String> playableList;
				if (toAdd.startsWith("."))
				{
					playableList = SceneFiles.list();
				}
				else if (toAdd.startsWith("-"))
				{
					playableList = new ArrayList<>();
					Recording.allContexts().forEach((ctx) -> playableList.add(ctx.id.str));
				}
				else
				{
					playableList = RecordingFiles.list();
				}

				if (playableList == null) { return false; }
				int successes = 0, matched = 0;
				for (String str : playableList)
				{
					if (str.startsWith(parts[0]) && str.endsWith(parts[1]))
					{
						SceneData.Subscene subscene = new SceneData.Subscene(str, PlaybackModifiers.empty());
						successes += SceneFiles.addElement(CommandOutput.LOGS, name, subscene) ? 1 : 0;
						matched++;
					}
				}

				if (matched == 0) { info.sendFailure("scenes.add_to.multiple.not_found"); }
				else if (successes == matched) { info.sendSuccess("scenes.add_to.multiple.success"); }
				else { info.sendFailure("scenes.add_to.multiple.error"); }
				return (successes == matched && matched != 0);
			}
		}
		catch (IllegalArgumentException e) { return info.sendException(e, "error.unable_to_get_argument"); }
	}

	private static boolean addTo(FullCommandInfo info)
	{
		try
		{
			String name = info.getString("scene_name");
			String toAdd = info.getString("to_add");

			if (toAdd.contains("*"))
			{
				info.sendFailure("scenes.add_to.multiple.pattern_with_arguments");
				return false;
			}

			double delay = 0.0;
			try
			{
				delay = info.getDouble("start_delay");
			}
			catch (IllegalArgumentException ignore) {}

			PlaybackModifiers modifiers = info.getSimpleModifiers(info);
			if (modifiers == null) { return false; }
			modifiers.startDelay = StartDelay.fromSeconds(delay);

			SceneData.Subscene subscene = new SceneData.Subscene(toAdd, modifiers);
			return SceneFiles.addElement(info, name, subscene);
		}
		catch (IllegalArgumentException e) { return info.sendException(e, "error.unable_to_get_argument"); }
	}

	private static boolean modify(FullCommandInfo info)
	{
		try
		{
			String name = info.getString("scene_name");
			Pair<Integer, String> posPair = CommandUtils.splitPosStr(info.getString("to_modify"));
			return SceneFiles.modify(info, name, posPair.getFirst(), posPair.getSecond());
		}
		catch (IllegalArgumentException e)
		{
			info.sendException(e, "error.unable_to_get_argument");
			return false;
		}
	}

	public static boolean list(CommandOutput out)
	{
		StringBuilder scenesListStr = new StringBuilder();
		List<String> scenesList = SceneFiles.list();

		if (scenesList == null)
		{
			scenesListStr.append(" ").append(Utils.stringFromComponent("list.error"));
		}
		else if (!scenesList.isEmpty())
		{
			scenesList.forEach((name) -> scenesListStr.append(" ").append(name));
		}
		else
		{
			scenesListStr.append(" ").append(Utils.stringFromComponent("list.empty"));
		}

		return out.sendSuccess("scenes.list", new String(scenesListStr));
	}
}
