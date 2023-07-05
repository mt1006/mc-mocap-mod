package com.mt1006.mocap.mocap.files;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;

public class Files
{
	private static final String MOCAP_DIR_NAME = "mocap_files";
	private static final String RECORDINGS_DIR_NAME = "recordings";
	private static final String SCENE_DIR_NAME = "scenes";
	private static final String SKIN_DIR_NAME = "skins";
	private static final String SLIM_SKIN_DIR_NAME = "slim";

	private static final String CONFIG_FILE_NAME = "settings.mcmocap_conf";
	private static final String RECORDING_EXTENSION = ".mcmocap_rec";
	private static final String SCENE_EXTENSION = ".mcmocap_scene";
	private static final String SKIN_EXTENSION = ".png";
	public static final String SLIM_SKIN_PREFIX = "slim/";

	private static boolean directoriesInitialized = false;
	public static File mocapDirectory = null;
	public static File recordingsDirectory = null;
	public static File sceneDirectory = null;
	public static File skinDirectory = null;
	public static File slimSkinDirectory = null;

	public static boolean initAndCheck(CommandSourceStack commandSource, String name)
	{
		return initDirectories(commandSource) && checkIfProperName(commandSource, name);
	}

	public static boolean initAndCheck(MinecraftServer server, String name)
	{
		return initDirectories(server, null) && checkIfProperName(null, name);
	}

	private static boolean initDirectories(CommandSourceStack commandSource)
	{
		return initDirectories(commandSource.getServer(), commandSource);
	}

	public static boolean initDirectories(MinecraftServer server, @Nullable CommandSourceStack commandSource)
	{
		if (directoriesInitialized) { return true; }

		mocapDirectory = createDirectory(server.getWorldPath(LevelResource.ROOT).toFile(), MOCAP_DIR_NAME);
		recordingsDirectory = createDirectory(mocapDirectory, RECORDINGS_DIR_NAME);
		sceneDirectory = createDirectory(mocapDirectory, SCENE_DIR_NAME);
		skinDirectory = createDirectory(mocapDirectory, SKIN_DIR_NAME);
		slimSkinDirectory = createDirectory(skinDirectory, SLIM_SKIN_DIR_NAME);

		if (!mocapDirectory.isDirectory() || !recordingsDirectory.isDirectory() || !sceneDirectory.isDirectory()
				|| !skinDirectory.isDirectory() || !slimSkinDirectory.isDirectory())
		{
			if (commandSource != null) { Utils.sendFailure(commandSource, "mocap.error.failed_to_init_directories"); }
			return false;
		}

		directoriesInitialized = true;
		return true;
	}

	public static void deinitDirectories()
	{
		directoriesInitialized = false;
	}

	public static boolean checkIfProperName(@Nullable CommandSourceStack commandSource, String name)
	{
		if (name.length() < 1) { return false; }

		if (name.charAt(0) == '.')
		{
			if (commandSource != null)
			{
				Utils.sendFailure(commandSource, "mocap.error.improper_name");
				Utils.sendFailure(commandSource, "mocap.error.improper_name.dot_first");
			}
			return false;
		}

		for (char c : name.toCharArray())
		{
			if (!isAllowedInInputName(c))
			{
				if (commandSource != null)
				{
					Utils.sendFailure(commandSource, "mocap.error.improper_name");
					Utils.sendFailure(commandSource, "mocap.error.improper_name.character");
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

	public static @Nullable File getSettingsFile(CommandSourceStack commandSource)
	{
		if (!initDirectories(commandSource)) { return null; }
		return new File(mocapDirectory, CONFIG_FILE_NAME);
	}

	public static @Nullable File getRecordingFile(CommandSourceStack commandSource, String name)
	{
		if (!initAndCheck(commandSource, name)) { return null; }
		return new File(recordingsDirectory, name + RECORDING_EXTENSION);
	}

	public static @Nullable File getSceneFile(CommandSourceStack commandSource, String name)
	{
		if (name.charAt(0) == '.') { name = name.substring(1); }
		if (!initAndCheck(commandSource, name)) { return null; }
		return new File(sceneDirectory, name + SCENE_EXTENSION);
	}

	public static @Nullable File getSkinFile(MinecraftServer server, String name)
	{
		boolean slimModel = false;
		if (name.startsWith(SLIM_SKIN_PREFIX))
		{
			name = name.substring(SLIM_SKIN_PREFIX.length());
			slimModel = true;
		}

		if (!initAndCheck(server, name)) { return null; }
		return new File(slimModel ? slimSkinDirectory : skinDirectory, name + SKIN_EXTENSION);
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

	public static boolean isAllowedInInputName(char c)
	{
		return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.';
	}

	private static File createDirectory(File parent, String name)
	{
		File directory = new File(parent, name);
		if (!directory.exists())
		{
			if (!directory.mkdir()) { MocapMod.LOGGER.warn("Failed to create directory: " + name); }
		}
		return directory;
	}
}
