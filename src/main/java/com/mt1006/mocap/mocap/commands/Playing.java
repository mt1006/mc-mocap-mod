package com.mt1006.mocap.mocap.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mt1006.mocap.mocap.playing.PlayedScene;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Playing
{
	private static List<PlayedScene> playedScenes =
			Collections.synchronizedList(new LinkedList<>());

	public static int start(CommandSourceStack commandSource, String name)
	{
		PlayedScene scene = new PlayedScene();
		if (!scene.start(commandSource, name, getNextID())) { return 0; }
		playedScenes.add(scene);
		commandSource.sendSuccess(new TranslatableComponent("mocap.commands.playing.start.success"), false);
		return 1;
	}

	public static int stop(CommandSourceStack commandSource, int id)
	{
		for (PlayedScene scene : playedScenes)
		{
			if (scene.getID() == id)
			{
				scene.stop();
				commandSource.sendSuccess(new TranslatableComponent("mocap.commands.playing.stop.success"), false);
				return 1;
			}
		}

		commandSource.sendFailure(new TranslatableComponent("mocap.commands.playing.stop.unable_to_find_scene"));
		return 0;
	}

	public static int stopAll(CommandContext<CommandSourceStack> ctx)
	{
		for (PlayedScene scene : playedScenes)
		{
			scene.stop();
		}

		ctx.getSource().sendSuccess(new TranslatableComponent("mocap.commands.playing.stop_all.success"), false);
		return 1;
	}

	public static int list(CommandContext<CommandSourceStack> ctx)
	{
		CommandSourceStack commandSource = ctx.getSource();

		commandSource.sendSuccess(new TranslatableComponent("mocap.commands.playing.list.playing"), false);

		for (PlayedScene scene : playedScenes)
		{
			commandSource.sendSuccess(new TextComponent(String.format("[%d] %s", scene.getID(), scene.getName())), false);
		}

		return 1;
	}

	public static void onTick()
	{
		ArrayList<PlayedScene> toRemove = new ArrayList<>();

		for (PlayedScene scene : playedScenes)
		{
			if (scene.finished) { toRemove.add(scene); }
			else { scene.onTick(); }
		}

		playedScenes.removeAll(toRemove);
	}

	private static int getNextID()
	{
		int maxInt = 1;
		for (PlayedScene scene : playedScenes)
		{
			if (scene.getID() >= maxInt)
			{
				maxInt = scene.getID() + 1;
			}
		}

		return maxInt;
	}
}
