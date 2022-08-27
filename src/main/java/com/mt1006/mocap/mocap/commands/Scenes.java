package com.mt1006.mocap.mocap.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mt1006.mocap.mocap.playing.SceneInfo;
import com.mt1006.mocap.utils.FileUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;

public class Scenes
{
	public static int listElements(CommandSource commandSource, String name)
	{
		SceneInfo sceneInfo = new SceneInfo();
		if(!sceneInfo.load(commandSource, name)) { return 0; }

		commandSource.sendSuccess(new TranslationTextComponent("mocap.commands.scenes.list_elements.list"), false);

		int i = 1;
		for (SceneInfo.Subscene element : sceneInfo.subscenes)
		{
			commandSource.sendSuccess(new StringTextComponent(String.format("[%d] %s <%f> [%f; %f; %f] (%s)",
					i, element.getName(), element.startDelay, element.startPos[0], element.startPos[1], element.startPos[2],
					element.playerName)), false);
			i++;
		}

		commandSource.sendSuccess(new StringTextComponent("[id] name <startDelay> [x; y; z] (playerName)"), false);
		return 1;
	}

	public static int list(CommandContext<CommandSource> ctx)
	{
		CommandSource commandSource = ctx.getSource();

		StringBuilder scenesListStr = new StringBuilder();
		ArrayList<String> scenesList = FileUtils.scenesList(commandSource);

		if (scenesList == null)
		{
			scenesListStr.append(" ");
			scenesListStr.append(new TranslationTextComponent("mocap.commands.playing.list.error").getString());
		}
		else if (!scenesList.isEmpty())
		{
			for (String name : scenesList)
			{
				scenesListStr.append(" .");
				scenesListStr.append(name);
			}
		}
		else
		{
			scenesListStr.append(" ");
			scenesListStr.append(new TranslationTextComponent("mocap.commands.playing.list.empty").getString());
		}

		commandSource.sendSuccess(new TranslationTextComponent("mocap.commands.playing.list.scenes",
				new String(scenesListStr)), false);
		return 1;
	}
}
