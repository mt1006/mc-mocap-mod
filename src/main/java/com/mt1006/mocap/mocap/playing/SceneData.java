package com.mt1006.mocap.mocap.playing;

import com.mt1006.mocap.mocap.files.Files;
import com.mt1006.mocap.mocap.files.SceneFiles;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class SceneData
{
	public final ArrayList<Subscene> subscenes = new ArrayList<>();
	public int version = 0;
	public long fileSize = 0;

	public boolean load(CommandSourceStack commandSource, String name)
	{
		byte[] data = Files.loadFile(Files.getSceneFile(commandSource, name));
		if (data == null) { return false; }
		return load(commandSource, data);
	}

	public boolean load(CommandSourceStack commandSource, byte[] scene)
	{
		fileSize = scene.length;

		try (Scanner scanner = new Scanner(new ByteArrayInputStream(scene)))
		{
			version = Integer.parseInt(scanner.next());
			if (version > SceneFiles.SCENE_VERSION)
			{
				Utils.sendFailure(commandSource, "mocap.error.failed_to_load_scene");
				Utils.sendFailure(commandSource, "mocap.error.failed_to_load_scene.not_supported");
				scanner.close();
				return false;
			}

			if (scanner.hasNextLine()) { scanner.nextLine(); }

			while (scanner.hasNextLine())
			{
				subscenes.add(new Subscene(new Scanner(scanner.nextLine())));
			}
			return true;
		}
		catch (Exception exception)
		{
			Utils.sendException(exception, commandSource, "mocap.error.failed_to_load_scene");
			return false;
		}
	}

	public static class Subscene
	{
		private static final PlayerData EMPTY_PLAYER_DATA = new PlayerData((String)null);
		public String name;
		public double startDelay = 0.0;
		public double[] posOffset = new double[3];
		public PlayerData playerData = EMPTY_PLAYER_DATA;
		public @Nullable String playerAsEntityID = null;

		public Subscene(String name)
		{
			this.name = name;
			posOffset[0] = 0.0;
			posOffset[1] = 0.0;
			posOffset[2] = 0.0;
		}

		public Subscene(Scanner scanner)
		{
			name = scanner.next();
			try
			{
				startDelay = Double.parseDouble(scanner.next());
				posOffset[0] = Double.parseDouble(scanner.next());
				posOffset[1] = Double.parseDouble(scanner.next());
				posOffset[2] = Double.parseDouble(scanner.next());
				playerData = new PlayerData(scanner);
				playerAsEntityID = Utils.toNullableStr(scanner.next());
			}
			catch (Exception ignore) {}
		}

		public Subscene copy()
		{
			return new Subscene(new Scanner(sceneToStr()));
		}

		public String sceneToStr()
		{
			return String.format(Locale.US, "%s %f %f %f %f %s %s", name, startDelay,
					posOffset[0], posOffset[1], posOffset[2], playerData.dataToStr(),
					Utils.toNotNullStr(playerAsEntityID));
		}
	}
}
