package com.mt1006.mocap.mocap.playing;

import com.mt1006.mocap.utils.FileUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class SceneInfo
{
	public static class Subscene
	{
		public SceneType type;
		private String name;
		public double startDelay = 0.0;
		public double[] startPos = new double[3];
		public String playerName = null;

		public Subscene(String name)
		{
			if (name.charAt(0) == '.')
			{
				this.name = name.substring(1);
				type = SceneType.SCENE;
			}
			else
			{
				this.name = name;
				type = SceneType.RECORDING;
			}

			startPos[0] = 0.0;
			startPos[1] = 0.0;
			startPos[2] = 0.0;
		}

		public Subscene(Scanner scanner)
		{
			String name = scanner.next();
			if (name.charAt(0) == '.')
			{
				this.name = name.substring(1);
				type = SceneType.SCENE;
			}
			else
			{
				this.name = name;
				type = SceneType.RECORDING;
			}

			startDelay = Double.parseDouble(scanner.next());
			startPos[0] = Double.parseDouble(scanner.next());
			startPos[1] = Double.parseDouble(scanner.next());
			startPos[2] = Double.parseDouble(scanner.next());

			playerName = scanner.next();
			if (playerName.equals("[null]") || playerName.length() > 16) { playerName = null; }
		}

		public String sceneToStr()
		{
			String outPlayerName;
			if (playerName == null) { outPlayerName = "[null]"; }
			else { outPlayerName = playerName; }

			return String.format(Locale.US, "%s %f %f %f %f %s", getName(), startDelay,
					startPos[0], startPos[1], startPos[2], outPlayerName);
		}

		public String getName()
		{
			if (type == SceneType.SCENE) { return "." + name; }
			else { return name; }
		}
	}

	public ArrayList<Subscene> subscenes = new ArrayList<>();

	public boolean load(CommandSourceStack commandSource, String name)
	{
		byte[] scene = FileUtils.loadScene(commandSource, name);
		if (scene == null) { return false; }
		return load(commandSource, scene);
	}

	public boolean load(CommandSourceStack commandSource, byte[] scene)
	{
		Scanner scanner = new Scanner(new ByteArrayInputStream(scene));

		try
		{
			int fileVersion = Integer.parseInt(scanner.next());
			if (fileVersion != FileUtils.SCENES_VERSION)
			{
				commandSource.sendFailure(Component.translatable("mocap.commands.error.failed_to_load_scene"));
				commandSource.sendFailure(Component.translatable("mocap.commands.error.failed_to_load_scene.not_supported"));
				scanner.close();
				return false;
			}

			while (scanner.hasNextLine()) { subscenes.add(new Subscene(scanner)); }
		}
		catch (Exception exception)
		{
			commandSource.sendFailure(Component.translatable("mocap.commands.error.failed_to_load_scene"));
			scanner.close();
			return false;
		}

		scanner.close();
		return true;
	}
}
