package net.mt1006.mocap.mocap.files;

import net.minecraft.world.level.storage.LevelResource;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.command.io.CommandOutput;
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

	private static final String CONFIG_FILE_NAME = "settings.txt";
	private static final String SCENE_ELEMENT_CACHE = "scene_element_cache";
	private static final String RECORDING_EXTENSION = ".mcmocap_rec";
	private static final String SCENE_EXTENSION = ".mcmocap_scene";
	private static final String SKIN_EXTENSION = ".png";
	public static final String SLIM_SKIN_PREFIX = "slim/";

	public static boolean initialized = false;
	public static File mocapDirectory = null;
	public static File recordingsDirectory = null;
	public static File sceneDirectory = null;
	public static File skinDirectory = null;
	public static File slimSkinDirectory = null;

	public static void init()
	{
		if (initialized) { return; }
		if (MocapMod.server == null)
		{
			throw new RuntimeException("Failed to init directories - server is null!");
		}

		mocapDirectory = createDirectory(MocapMod.server.getWorldPath(LevelResource.ROOT).toFile(), MOCAP_DIR_NAME);
		recordingsDirectory = createDirectory(mocapDirectory, RECORDINGS_DIR_NAME);
		sceneDirectory = createDirectory(mocapDirectory, SCENE_DIR_NAME);
		skinDirectory = createDirectory(mocapDirectory, SKIN_DIR_NAME);
		slimSkinDirectory = createDirectory(skinDirectory, SLIM_SKIN_DIR_NAME);

		if (!mocapDirectory.isDirectory() || !recordingsDirectory.isDirectory() || !sceneDirectory.isDirectory()
				|| !skinDirectory.isDirectory() || !slimSkinDirectory.isDirectory())
		{
			return;
		}

		initialized = true;
	}

	public static void deinit()
	{
		initialized = false;
	}

	public static boolean check(CommandOutput commandOutput, String name)
	{
		return checkIfInitialized(commandOutput) && checkIfProperName(commandOutput, name);
	}

	public static boolean checkIfInitialized(CommandOutput commandOutput)
	{
		if (!initialized) { commandOutput.sendFailure("error.failed_to_init_directories"); }
		return initialized;
	}

	public static boolean checkIfProperName(CommandOutput commandOutput, String name)
	{
		if (name.isEmpty()) { return false; }

		if (name.charAt(0) == '.')
		{
			commandOutput.sendFailure("failure.improper_filename");
			commandOutput.sendFailure("failure.improper_filename.dot_first");
			return false;
		}
		if (name.charAt(0) == '-')
		{
			commandOutput.sendFailure("failure.improper_filename");
			commandOutput.sendFailure("failure.improper_filename.dash_first");
			return false;
		}

		for (char c : name.toCharArray())
		{
			if (!isAllowedInInputName(c))
			{
				commandOutput.sendFailure("failure.improper_filename");
				commandOutput.sendFailure("failure.improper_filename.character");
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
		catch (Exception e) { return null; }

		return data;
	}

	public static @Nullable File getSettingsFile()
	{
		return initialized ? new File(mocapDirectory, CONFIG_FILE_NAME) : null;
	}

	public static @Nullable File getSceneElementCache()
	{
		return initialized ? new File(mocapDirectory, SCENE_ELEMENT_CACHE) : null;
	}

	public static @Nullable File getRecordingFile(CommandOutput commandOutput, String name)
	{
		return check(commandOutput, name) ? new File(recordingsDirectory, name + RECORDING_EXTENSION) : null;
	}

	public static @Nullable File getSceneFile(CommandOutput commandOutput, String name)
	{
		if (name.charAt(0) == '.') { name = name.substring(1); }
		return check(commandOutput, name) ? new File(sceneDirectory, name + SCENE_EXTENSION) : null;
	}

	public static @Nullable File getSkinFile(String name)
	{
		boolean slimModel = false;
		if (name.startsWith(SLIM_SKIN_PREFIX))
		{
			name = name.substring(SLIM_SKIN_PREFIX.length());
			slimModel = true;
		}

		if (!check(CommandOutput.DUMMY, name)) { return null; }
		return new File(slimModel ? slimSkinDirectory : skinDirectory, name + SKIN_EXTENSION);
	}

	public static boolean isRecordingFile(File directory, String name)
	{
		File file = new File(directory, name);
		return !file.isDirectory() && name.endsWith(RECORDING_EXTENSION) && checkIfProperName(CommandOutput.DUMMY, name);
	}

	public static boolean isSceneFile(File directory, String name)
	{
		File file = new File(directory, name);
		return !file.isDirectory() && name.endsWith(SCENE_EXTENSION) && checkIfProperName(CommandOutput.DUMMY, name);
	}

	public static boolean isSkinFile(File directory, String name)
	{
		File file = new File(directory, name);
		return !file.isDirectory() && name.endsWith(SKIN_EXTENSION) && checkIfProperName(CommandOutput.DUMMY, name);
	}

	public static boolean isAllowedInInputName(int c)
	{
		return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.';
	}

	public static boolean printVersionInfo(CommandOutput commandOutput, int currentVersion, int fileVersion, boolean isFileExperimental)
	{
		String suffix = isFileExperimental ? ".experimental" : "";

		if (fileVersion > currentVersion)
		{
			commandOutput.sendSuccess("file.info.version.not_supported" + suffix, fileVersion);
			return false;
		}

		if (fileVersion == currentVersion) { commandOutput.sendSuccess("file.info.version.current" + suffix, fileVersion); }
		else if (fileVersion > 0) { commandOutput.sendSuccess("file.info.version.old" + suffix, fileVersion); }
		else { commandOutput.sendSuccess("file.info.version.undefined", fileVersion); }
		return true;
	}

	private static File createDirectory(File parent, String name)
	{
		File directory = new File(parent, name);
		if (!directory.exists())
		{
			if (!directory.mkdir()) { MocapMod.LOGGER.warn("Failed to create directory: {}", name); }
		}
		return directory;
	}
}
