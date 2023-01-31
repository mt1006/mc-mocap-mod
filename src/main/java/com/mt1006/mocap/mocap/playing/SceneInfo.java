package com.mt1006.mocap.mocap.playing;

import com.mt1006.mocap.mocap.files.Files;
import com.mt1006.mocap.mocap.files.SceneFile;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class SceneInfo
{
	public final ArrayList<Subscene> subscenes = new ArrayList<>();

	public boolean load(CommandSourceStack commandSource, String name)
	{
		byte[] data = Files.loadFile(Files.getSceneFile(commandSource, name));
		if (data == null) { return false; }
		return load(commandSource, data);
	}

	public boolean load(CommandSourceStack commandSource, byte[] scene)
	{
		try (Scanner scanner = new Scanner(new ByteArrayInputStream(scene)))
		{
			int fileVersion = Integer.parseInt(scanner.next());
			if (fileVersion > SceneFile.SCENE_VERSION)
			{
				Utils.sendFailure(commandSource, "mocap.commands.error.failed_to_load_scene");
				Utils.sendFailure(commandSource, "mocap.commands.error.failed_to_load_scene.not_supported");
				scanner.close();
				return false;
			}

			if (scanner.hasNextLine()) { scanner.nextLine(); }

			while (scanner.hasNextLine())
			{
				subscenes.add(new Subscene(new Scanner(scanner.nextLine())));
			}
		}
		catch (Exception exception)
		{
			Utils.sendFailure(commandSource, "mocap.commands.error.failed_to_load_scene");
			return false;
		}
		return true;
	}

	public static class Subscene
	{
		public final String name;
		public double startDelay = 0.0;
		public double[] startPos = new double[3];
		public String playerName = null;
		public @Nullable String mineskinURL = null;

		public Subscene(String name)
		{
			this.name = name;
			startPos[0] = 0.0;
			startPos[1] = 0.0;
			startPos[2] = 0.0;
		}

		public Subscene(Scanner scanner)
		{
			name = scanner.next();

			try
			{
				startDelay = Double.parseDouble(scanner.next());
				startPos[0] = Double.parseDouble(scanner.next());
				startPos[1] = Double.parseDouble(scanner.next());
				startPos[2] = Double.parseDouble(scanner.next());

				playerName = scanner.next();
				if (playerName.equals("[null]") || playerName.length() > 16) { playerName = null; }

				mineskinURL = scanner.next();
				if (mineskinURL.equals("[null]")) { mineskinURL = null; }
			}
			catch (Exception ignore) {}
		}

		public String sceneToStr()
		{
			String outPlayerName = playerName != null ? playerName : "[null]";
			String outMineskinURL = mineskinURL != null ? mineskinURL : "[null]";

			return String.format(Locale.US, "%s %f %f %f %f %s %s", name, startDelay,
					startPos[0], startPos[1], startPos[2], outPlayerName, outMineskinURL);
		}
	}
}
