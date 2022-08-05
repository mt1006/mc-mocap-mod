package com.mt1006.mocap.mocap;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Playing
{
	private static List<PlayedScene> playedScenes =
			Collections.synchronizedList(new LinkedList<>());

	public static int start(CommandSource commandSource, String name)
	{
		PlayedScene scene = new PlayedScene();
		if (!scene.start(commandSource, name, getNextID())) { return 0; }
		playedScenes.add(scene);
		commandSource.sendSuccess(new TranslationTextComponent("mocap.commands.playing.start.success"), false);
		return 1;
	}

	public static int stop(CommandSource commandSource, int id)
	{
		for (PlayedScene scene : playedScenes)
		{
			if (scene.getID() == id)
			{
				scene.stop();
				commandSource.sendSuccess(new TranslationTextComponent("mocap.commands.playing.stop.success"), false);
				return 1;
			}
		}

		commandSource.sendFailure(new TranslationTextComponent("mocap.commands.playing.stop.unable_to_find_scene"));
		return 0;
	}

	public static int stopAll(CommandContext<CommandSource> ctx)
	{
		for (PlayedScene scene : playedScenes)
		{
			scene.stop();
		}

		ctx.getSource().sendSuccess(new TranslationTextComponent("mocap.commands.playing.stop_all.success"), false);
		return 1;
	}

	public static int list(CommandContext<CommandSource> ctx)
	{
		CommandSource commandSource = ctx.getSource();

		commandSource.sendSuccess(new TranslationTextComponent("mocap.commands.playing.list.playing"), false);

		for (PlayedScene scene : playedScenes)
		{
			commandSource.sendSuccess(new StringTextComponent(String.format("[%d] %s", scene.getID(), scene.getName())), false);
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
