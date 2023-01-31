package com.mt1006.mocap.mocap.files;

import com.mt1006.mocap.utils.Utils;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.FolderName;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;

public class Files
{
	private static final String MOCAP_DIR_NAME = "mocap_files";
	private static final String RECORDINGS_DIR_NAME = "recordings";
	private static final String SCENES_DIR_NAME = "scenes";

	private static final String CONFIG_FILE_NAME = "settings.mcmocap_conf";
	private static final String RECORDING_EXTENSION = ".mcmocap_rec";
	private static final String SCENE_EXTENSION = ".mcmocap_scene";

	private static boolean directoriesInitialized = false;
	public static File mocapDirectory = null;
	public static File recordingsDirectory = null;
	public static File scenesDirectory = null;

	public static boolean initAndCheck(CommandSource commandSource, String name)
	{
		return initDirectories(commandSource) && checkIfProperName(commandSource, name);
	}

	public static boolean initDirectories(CommandSource commandSource)
	{
		if (directoriesInitialized) { return true; }

		MinecraftServer server = commandSource.getServer();

		mocapDirectory = new File(server.getWorldPath(FolderName.ROOT).toFile(), MOCAP_DIR_NAME);
		if (!mocapDirectory.exists()) { mocapDirectory.mkdir(); }

		recordingsDirectory = new File(mocapDirectory, RECORDINGS_DIR_NAME);
		if (!recordingsDirectory.exists()) { recordingsDirectory.mkdir(); }

		scenesDirectory = new File(mocapDirectory, SCENES_DIR_NAME);
		if (!scenesDirectory.exists()) { scenesDirectory.mkdir(); }

		if (!mocapDirectory.isDirectory() || !recordingsDirectory.isDirectory() || !scenesDirectory.isDirectory())
		{
			Utils.sendFailure(commandSource, "mocap.commands.error.failed_to_init_directories");
			return false;
		}

		directoriesInitialized = true;
		return true;
	}

	public static void deinitDirectories()
	{
		directoriesInitialized = false;
	}

	private static boolean checkIfProperName(@Nullable CommandSource commandSource, String name)
	{
		if (name.length() < 1) { return false; }

		if (name.charAt(0) == '.')
		{
			if (commandSource != null)
			{
				Utils.sendFailure(commandSource, "mocap.commands.error.improper_name");
				Utils.sendFailure(commandSource, "mocap.commands.error.improper_name.dot_first");
			}
			return false;
		}

		for (char c : name.toCharArray())
		{
			if (!(c >= 'a' && c <= 'z') && !(c >= '0' && c <= '9') && c != '_' && c != '-' && c != '.')
			{
				if (commandSource != null)
				{
					Utils.sendFailure(commandSource, "mocap.commands.error.improper_name");
					Utils.sendFailure(commandSource, "mocap.commands.error.improper_name.character");
				}
				return false;
			}
		}

		return true;
	}

	public static byte @Nullable[] loadFile(@Nullable File file)
	{
		if (file == null) { return null; }
		byte[] data;

		try (FileInputStream stream = new FileInputStream(file))
		{
			int fileSize = (int)file.length();
			data = new byte[fileSize];
			if (stream.read(data) != fileSize) { return null; }
		}
		catch (Exception exception)
		{
			return null;
		}

		return data;
	}

	public static @Nullable File getSettingsFile(CommandSource commandSource)
	{
		if (!initDirectories(commandSource)) { return null; }
		return new File(mocapDirectory, CONFIG_FILE_NAME);
	}

	public static @Nullable File getRecordingFile(CommandSource commandSource, String name)
	{
		if (!initAndCheck(commandSource, name)) { return null; }
		return new File(recordingsDirectory, name + RECORDING_EXTENSION);
	}

	public static @Nullable File getSceneFile(CommandSource commandSource, String name)
	{
		if (name.charAt(0) == '.') { name = name.substring(1); }
		if (!initAndCheck(commandSource, name)) { return null; }
		return new File(scenesDirectory, name + SCENE_EXTENSION);
	}

	public static boolean isRecordingFile(String name)
	{
		File file = new File(name);
		return !file.isDirectory() && name.endsWith(RECORDING_EXTENSION) && checkIfProperName(null, name);
	}

	public static boolean isSceneFile(String name)
	{
		File file = new File(name);
		return !file.isDirectory() && name.endsWith(SCENE_EXTENSION) && checkIfProperName(null, name);
	}
}
