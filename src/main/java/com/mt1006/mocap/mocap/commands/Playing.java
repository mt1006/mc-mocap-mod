package com.mt1006.mocap.mocap.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mt1006.mocap.mocap.playing.PlayedScene;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Playing
{
	private static final List<PlayedScene> playedScenes = Collections.synchronizedList(new LinkedList<>());
	private static long tickCounter = 0;
	private static double timer = 0.0;
	private static double previousPlayingSpeed = 0.0;

	public static int start(CommandSource commandSource, String name, String playerName, String mineskinURL)
	{
		if (!Settings.loaded) { Settings.load(commandSource); }

		PlayedScene scene = new PlayedScene();
		if (!scene.start(commandSource, name, playerName, mineskinURL, getNextID())) { return 0; }
		playedScenes.add(scene);

		Utils.sendSuccess(commandSource, "mocap.commands.playing.start.success");
		return 1;
	}

	public static void stop(CommandSource commandSource, int id)
	{
		for (PlayedScene scene : playedScenes)
		{
			if (scene.getID() == id)
			{
				scene.stop();
				Utils.sendSuccess(commandSource, "mocap.commands.playing.stop.success");
				return;
			}
		}

		Utils.sendFailure(commandSource, "mocap.commands.playing.stop.unable_to_find_scene");
		Utils.sendFailure(commandSource, "mocap.commands.playing.stop.unable_to_find_scene.tip");
	}

	public static int stopAll(@Nullable CommandContext<CommandSource> ctx)
	{
		for (PlayedScene scene : playedScenes)
		{
			scene.stop();
		}

		if (ctx != null) { Utils.sendSuccess(ctx.getSource(), "mocap.commands.playing.stop_all.success"); }
		return 1;
	}

	public static int list(CommandContext<CommandSource> ctx)
	{
		CommandSource commandSource = ctx.getSource();
		Utils.sendSuccess(ctx.getSource(), "mocap.commands.playing.list.playing");

		for (PlayedScene scene : playedScenes)
		{
			Utils.sendSuccessLiteral(commandSource, "[%d] %s", scene.getID(), scene.getName());
		}
		return 1;
	}

	public static void onTick()
	{
		if (!playedScenes.isEmpty())
		{
			if (previousPlayingSpeed != Settings.PLAYING_SPEED.val)
			{
				timer = 0.0;
				previousPlayingSpeed = Settings.PLAYING_SPEED.val;
			}

			if ((long)timer < tickCounter) { timer = tickCounter; }

			while ((long)timer == tickCounter)
			{
				ArrayList<PlayedScene> toRemove = new ArrayList<>();

				for (PlayedScene scene : playedScenes)
				{
					if (scene.finished) { toRemove.add(scene); }
					else { scene.onTick(); }
				}

				playedScenes.removeAll(toRemove);
				if (playedScenes.isEmpty()) { break; }

				timer += 1.0 / Settings.PLAYING_SPEED.val;
			}
		}

		tickCounter++;
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
