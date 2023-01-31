package com.mt1006.mocap.mocap.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mt1006.mocap.mocap.files.SceneFile;
import com.mt1006.mocap.mocap.playing.SceneInfo;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;

public class Scenes
{
	public static int listElements(CommandSourceStack commandSource, String name)
	{
		SceneInfo sceneInfo = new SceneInfo();
		if(!sceneInfo.load(commandSource, name)) { return 0; }

		Utils.sendSuccess(commandSource, "mocap.commands.scenes.list_elements.list");

		int i = 1;
		for (SceneInfo.Subscene element : sceneInfo.subscenes)
		{
			Utils.sendSuccessLiteral(commandSource, "[%d] %s <%f> [%f; %f; %f] (%s)", i++, element.name,
					element.startDelay, element.startPos[0], element.startPos[1], element.startPos[2], element.playerName);

			if (element.mineskinURL != null)
			{
				Utils.sendSuccessComponent(commandSource, Utils.getURLComponent(element.mineskinURL, "   (§n%s§r)", element.mineskinURL));
			}
		}

		Utils.sendSuccessLiteral(commandSource, "[id] name <startDelay> [x; y; z] (playerName) (mineskinURL)");
		return 1;
	}

	public static int list(CommandContext<CommandSourceStack> ctx)
	{
		CommandSourceStack commandSource = ctx.getSource();

		StringBuilder scenesListStr = new StringBuilder();
		List<String> scenesList = SceneFile.list(commandSource);

		if (scenesList == null)
		{
			scenesListStr.append(" ").append(Utils.stringFromComponent("mocap.commands.playing.list.error"));
		}
		else if (!scenesList.isEmpty())
		{
			for (String name : scenesList)
			{
				scenesListStr.append(" .").append(name);
			}
		}
		else
		{
			scenesListStr.append(" ").append(Utils.stringFromComponent("mocap.commands.playing.list.empty"));
		}

		Utils.sendSuccess(commandSource, "mocap.commands.playing.list.scenes", new String(scenesListStr));
		return 1;
	}
}
