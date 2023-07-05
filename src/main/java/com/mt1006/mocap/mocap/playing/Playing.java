package com.mt1006.mocap.mocap.playing;

import com.mt1006.mocap.mocap.settings.Settings;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Playing
{
	public static final String MOCAP_ENTITY_TAG = "mocap_entity";
	public static final List<PlayedScene> playedScenes = Collections.synchronizedList(new LinkedList<>());
	private static long tickCounter = 0;
	private static double timer = 0.0;
	private static double previousPlayingSpeed = 0.0;

	public static int start(CommandSourceStack commandSource, String name, PlayerData playerData)
	{
		if (!Settings.loaded) { Settings.load(commandSource); }

		PlayedScene scene = new PlayedScene();
		if (!scene.start(commandSource, name, playerData, getNextID())) { return 0; }
		playedScenes.add(scene);

		Utils.sendSuccess(commandSource, "mocap.playing.start.success");
		return 1;
	}

	public static void stop(CommandSourceStack commandSource, int id)
	{
		for (PlayedScene scene : playedScenes)
		{
			if (scene.getID() == id)
			{
				scene.stop();
				Utils.sendSuccess(commandSource, "mocap.playing.stop.success");
				return;
			}
		}

		Utils.sendFailure(commandSource, "mocap.playing.stop.unable_to_find_scene");
		Utils.sendFailure(commandSource, "mocap.playing.stop.unable_to_find_scene.tip");
	}

	public static boolean stopAll(@Nullable CommandSourceStack commandSource)
	{
		playedScenes.forEach(PlayedScene::stop);
		if (commandSource != null) { Utils.sendSuccess(commandSource, "mocap.playing.stop_all.success"); }
		return true;
	}

	public static boolean list(CommandSourceStack commandSource)
	{
		Utils.sendSuccess(commandSource, "mocap.playing.list");

		for (PlayedScene scene : playedScenes)
		{
			Utils.sendSuccessLiteral(commandSource, "[%d] %s", scene.getID(), scene.getName());
		}
		return true;
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
					if (scene.isFinished()) { toRemove.add(scene); }
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
