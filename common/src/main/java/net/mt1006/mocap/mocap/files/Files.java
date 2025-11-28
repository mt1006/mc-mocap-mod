package net.mt1006.mocap.mocap.files;

import net.minecraft.world.level.storage.LevelResource;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.v1.io.CommandOutput;
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
	public static final String RECORDING_EXTENSION = ".mcmocap_rec";
	public static final String SCENE_EXTENSION = ".mcmocap_scene";
	private static final String SKIN_EXTENSION = ".png";
	public static final String SKIN_LIST_EXTENSION = ".txt";
	public static final String SLIM_SKIN_PREFIX = "slim/";
	public static final String SKIN_LIST_PREFIX = "list/";

	public static boolean initialized = false;
	public static File mocapDirectory = null;
	public static File recordingsDirectory = null;
	public static File sceneDirectory = null;
	public static File skinDirectory = null;
	public static File slimSkinDirectory = null;
	public static File skinListDirectory = null;

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
		skinListDirectory = createDirectory(skinDirectory, SKIN_LIST_PREFIX);

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

	public static boolean check(CommandOutput out, String name)
	{
		return checkIfInitialized(out) && checkIfProperName(out, name);
	}

	public static boolean checkIfInitialized(CommandOutput out)
	{
		if (!initialized) { out.sendFailure("error.failed_to_init_directories"); }
		return initialized;
	}

	public static boolean checkIfProperName(CommandOutput out, String name)
	{
		if (name.isEmpty()) { return false; }

		if (name.charAt(0) == '.')
		{
			out.sendFailure("failure.improper_filename");
			out.sendFailure("failure.improper_filename.dot_first");
			return false;
		}
		if (name.charAt(0) == '-')
		{
			out.sendFailure("failure.improper_filename");
			out.sendFailure("failure.improper_filename.dash_first");
			return false;
		}

		for (char c : name.toCharArray())
		{
			if (!isAllowedInInputName(c))
			{
				out.sendFailure("failure.improper_filename");
				out.sendFailure("failure.improper_filename.character");
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
		return isProperFile(directory, name, RECORDING_EXTENSION);
	}

	public static boolean isSceneFile(File directory, String name)
	{
		return isProperFile(directory, name, SCENE_EXTENSION);
	}

	public static boolean isSkinFile(File directory, String name)
	{
		return isProperFile(directory, name, SKIN_EXTENSION);
	}

	public static boolean isSkinListFile(File directory, String name)
	{
		return isProperFile(directory, name, SKIN_LIST_EXTENSION);
	}

	private static boolean isProperFile(File directory, String name, String extension)
	{
		File file = new File(directory, name);
		return !file.isDirectory() && name.endsWith(extension) && checkIfProperName(CommandOutput.DUMMY, name);
	}

	public static boolean isAllowedInInputName(int c)
	{
		return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.';
	}

	public static boolean printVersionInfo(CommandOutput out, int currentVersion, int fileVersion,
										   boolean isFileExperimental, int experimentalSubversion)
	{
		String suffix = isFileExperimental ? ".experimental" : "";
		String versionStr = fileVersion + (isFileExperimental ? ("-exp." + experimentalSubversion) : "");
		if (fileVersion > currentVersion) { return out.sendFailure("file.info.version.not_supported" + suffix, versionStr); }

		if (fileVersion == currentVersion) { out.sendSuccess("file.info.version.current" + suffix, versionStr); }
		else if (fileVersion > 0) { out.sendSuccess("file.info.version.old" + suffix, versionStr); }
		else { out.sendSuccess("file.info.version.undefined", versionStr); }
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
