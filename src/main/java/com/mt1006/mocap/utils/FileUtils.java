package com.mt1006.mocap.utils;

import com.mt1006.mocap.mocap.playing.SceneInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;

public class FileUtils
{
	public static final byte RECORDINGS_VERSION = 1;
	public static final int SCENES_VERSION = 1;

	private static final String MOCAP_DIR_NAME = "mocap_files";
	private static final String RECORDINGS_DIR_NAME = "recordings";
	private static final String SCENES_DIR_NAME = "scenes";
	private static final String RECORDINGS_EXTENSION = ".mcmocap_rec";
	private static final String SCENES_EXTENSION = ".mcmocap_scene";
	private static final String CONFIG_FILE_NAME = "settings.mcmocap_conf";

	private static boolean directoriesInitialized = false;
	private static File mocapDirectory = null;
	private static File recordingsDirectory = null;
	private static File scenesDirectory = null;

	public static boolean saveRecording(CommandSourceStack commandSource, String name, ArrayList<Byte> recording)
	{
		if (!initDirectories(commandSource) || !checkIfProperName(name, commandSource)) { return false; }

		File recordingFile = new File(recordingsDirectory, name + RECORDINGS_EXTENSION);

		try
		{
			if (recordingFile.exists())
			{
				commandSource.sendFailure(Component.translatable("mocap.commands.recording.save.file_already_exist"));
				commandSource.sendFailure(Component.translatable("mocap.commands.recording.save.file_already_exist.tip"));
				return false;
			}

			FileOutputStream outputStream = new FileOutputStream(recordingFile);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
			for(Byte val : recording)
			{
				bufferedOutputStream.write(new byte[] {val});
			}
			bufferedOutputStream.close();
			recording.clear();
		}
		catch (IOException exception)
		{
			commandSource.sendFailure(Component.translatable("mocap.commands.recording.save.exception"));
			return false;
		}

		commandSource.sendSuccess(Component.translatable("mocap.commands.recording.save.success"), false);
		return true;
	}

	public static boolean removeRecording(CommandSourceStack commandSource, String name)
	{
		if (!initDirectories(commandSource) || !checkIfProperName(name, commandSource)) { return false; }

		File recordingFile = new File(recordingsDirectory, name + RECORDINGS_EXTENSION);

		if (!recordingFile.delete())
		{
			commandSource.sendFailure(Component.translatable("mocap.commands.recording.remove.failed"));
			return false;
		}

		commandSource.sendSuccess(Component.translatable("mocap.commands.recording.remove.success"), false);
		return true;
	}

	@Nullable
	public static byte[] loadRecording(CommandSourceStack commandSource, String name)
	{
		if (!initDirectories(commandSource) || !checkIfProperName(name, commandSource)) { return null; }

		File recordingFile = new File(recordingsDirectory, name + RECORDINGS_EXTENSION);
		byte[] recording;

		try
		{
			FileInputStream inputStream = new FileInputStream(recordingFile);
			recording = new byte[(int)recordingFile.length()];
			inputStream.read(recording);
			inputStream.close();
		}
		catch (Exception exception)
		{
			return null;
		}

		return recording;
	}

	@Nullable
	public static ArrayList<String> recordingsList(CommandSourceStack commandSource)
	{
		if (!FileUtils.initDirectories(commandSource)) { return null; }

		ArrayList<String> recordings = new ArrayList<>();

		String[] filesList = recordingsDirectory.list();
		for (String filename : filesList)
		{
			File file = new File(filename);
			if (!file.isDirectory() && filename.endsWith(RECORDINGS_EXTENSION) &&
					checkIfProperName(filename, null))
			{
				recordings.add(filename.substring(0, filename.lastIndexOf('.')));
			}
		}

		return recordings;
	}

	public static boolean addScene(CommandSourceStack commandSource, String name)
	{
		if (!initDirectories(commandSource) || !checkIfProperName(name, commandSource)) { return false; }

		File sceneFile = new File(scenesDirectory, name + SCENES_EXTENSION);

		try
		{
			if (sceneFile.exists())
			{
				commandSource.sendFailure(Component.translatable("mocap.commands.scenes.add.error"));
				commandSource.sendFailure(Component.translatable("mocap.commands.scenes.add.error.file_already_exists"));
				return false;
			}

			PrintWriter printWriter = new PrintWriter(sceneFile);
			printWriter.print(String.format("%d", SCENES_VERSION));
			printWriter.close();
		}
		catch (IOException exception)
		{
			commandSource.sendFailure(Component.translatable("mocap.commands.scenes.add.error"));
			return false;
		}

		commandSource.sendSuccess(Component.translatable("mocap.commands.scenes.add.success"), false);
		return true;
	}

	public static boolean removeScene(CommandSourceStack commandSource, String name)
	{
		if (!initDirectories(commandSource) || !checkIfProperName(name, commandSource)) { return false; }

		File sceneFile = new File(scenesDirectory, name + SCENES_EXTENSION);

		if (!sceneFile.delete())
		{
			commandSource.sendFailure(Component.translatable("mocap.commands.scenes.remove.failed"));
			return false;
		}

		commandSource.sendSuccess(Component.translatable("mocap.commands.scenes.remove.success"), false);
		return true;
	}

	public static boolean addToScene(CommandSourceStack commandSource, String name, String lineToAdd)
	{
		if (!initDirectories(commandSource) || !checkIfProperName(name, commandSource)) { return false; }

		File sceneFile = new File(scenesDirectory, name + SCENES_EXTENSION);

		try
		{
			if (!sceneFile.exists())
			{
				commandSource.sendFailure(Component.translatable("mocap.commands.scenes.add_to.error"));
				commandSource.sendFailure(Component.translatable("mocap.commands.scenes.add_to.error.file_not_exists"));
				return false;
			}

			SceneInfo sceneInfo = new SceneInfo();
			if (!sceneInfo.load(commandSource, name)) { return false; }

			PrintWriter printWriter = new PrintWriter(sceneFile);
			printWriter.print(String.format("%d", SCENES_VERSION));
			for (SceneInfo.Subscene subscene : sceneInfo.subscenes)
			{
				printWriter.print("\n" + subscene.sceneToStr());
			}
			printWriter.print("\n" + lineToAdd);
			printWriter.close();
		}
		catch (IOException exception)
		{
			commandSource.sendFailure(Component.translatable("mocap.commands.scenes.add_to.error"));
			return false;
		}

		commandSource.sendSuccess(Component.translatable("mocap.commands.scenes.add_to.success"), false);
		return true;
	}

	public static boolean removeFromScene(CommandSourceStack commandSource, String name, int pos)
	{
		if (!initDirectories(commandSource) || !checkIfProperName(name, commandSource)) { return false; }

		File sceneFile = new File(scenesDirectory, name + SCENES_EXTENSION);

		try
		{
			if (!sceneFile.exists())
			{
				commandSource.sendFailure(Component.translatable("mocap.commands.scenes.remove_from.error"));
				commandSource.sendFailure(Component.translatable("mocap.commands.scenes.remove_from.error.file_not_exists"));
				return false;
			}

			SceneInfo sceneInfo = new SceneInfo();
			if (!sceneInfo.load(commandSource, name)) { return false; }

			if (sceneInfo.subscenes.size() < pos || pos < 1)
			{
				commandSource.sendFailure(Component.translatable("mocap.commands.scenes.remove_from.error"));
				commandSource.sendFailure(Component.translatable("mocap.commands.scenes.remove_from.error.wrong_element_pos"));
				commandSource.sendFailure(Component.translatable("mocap.commands.scenes.remove_from.error.wrong_element_pos.tip"));
				return false;
			}

			int i = 1;
			PrintWriter printWriter = new PrintWriter(sceneFile);
			printWriter.print(String.format("%d", SCENES_VERSION));
			for (SceneInfo.Subscene subscene : sceneInfo.subscenes)
			{
				if (i != pos)
				{
					printWriter.print("\n" + subscene.sceneToStr());
				}
				i++;
			}
			printWriter.close();
		}
		catch (IOException exception)
		{
			commandSource.sendFailure(Component.translatable("mocap.commands.scenes.remove_from.error"));
			return false;
		}

		commandSource.sendSuccess(Component.translatable("mocap.commands.scenes.remove_from.success"), false);
		return true;
	}

	@Nullable
	public static byte[] loadScene(CommandSourceStack commandSource, String name)
	{
		if (!initDirectories(commandSource) || !checkIfProperName(name, commandSource)) { return null; }

		File file = new File(scenesDirectory, name + SCENES_EXTENSION);
		byte[] scene;

		try
		{
			FileInputStream inputStream = new FileInputStream(file);
			scene = new byte[(int)file.length()];
			inputStream.read(scene);
			inputStream.close();
		}
		catch (Exception exception)
		{
			commandSource.sendFailure(Component.translatable("mocap.commands.error.failed_to_load_scene"));
			return null;
		}

		return scene;
	}

	@Nullable
	public static ArrayList<String> scenesList(CommandSourceStack commandSource)
	{
		if (!FileUtils.initDirectories(commandSource)) { return null; }

		ArrayList<String> scenes = new ArrayList<>();

		String[] filesList = scenesDirectory.list();
		for (String filename : filesList)
		{
			File file = new File(filename);
			if (!file.isDirectory() && filename.endsWith(SCENES_EXTENSION) &&
					checkIfProperName(filename, null))
			{
				scenes.add(filename.substring(0, filename.lastIndexOf('.')));
			}
		}

		return scenes;
	}

	@Nullable
	public static File getSettingsFile(CommandSourceStack commandSource)
	{
		if (!FileUtils.initDirectories(commandSource)) { return null; }
		return new File(mocapDirectory, CONFIG_FILE_NAME);
	}

	public static boolean initDirectories(CommandSourceStack commandSource)
	{
		if (directoriesInitialized) { return true; }

		MinecraftServer server = commandSource.getServer();

		mocapDirectory = new File(server.getWorldPath(LevelResource.ROOT).toFile(), MOCAP_DIR_NAME);
		if (!mocapDirectory.exists()) { mocapDirectory.mkdir(); }

		recordingsDirectory = new File(mocapDirectory, RECORDINGS_DIR_NAME);
		if (!recordingsDirectory.exists()) { recordingsDirectory.mkdir(); }

		scenesDirectory = new File(mocapDirectory, SCENES_DIR_NAME);
		if (!scenesDirectory.exists()) { scenesDirectory.mkdir(); }

		if (!mocapDirectory.isDirectory() || !recordingsDirectory.isDirectory() || !scenesDirectory.isDirectory())
		{
			commandSource.sendFailure(Component.translatable("mocap.commands.error.failed_to_init_directories"));
			return false;
		}

		directoriesInitialized = true;
		return true;
	}

	private static boolean checkIfProperName(String name, @Nullable CommandSourceStack commandSource)
	{
		if (name.length() < 1) { return false; }

		if (name.charAt(0) == '.')
		{
			commandSource.sendFailure(Component.translatable("mocap.commands.error.improper_name"));
			commandSource.sendFailure(Component.translatable("mocap.commands.error.improper_name.dot_first"));
			return false;
		}

		for (char c : name.toCharArray())
		{
			if (!(c >= 'a' && c <= 'z') && !(c >= '0' && c <= '9') &&
					c != '_' && c != '-' && c != '.')
			{
				if (commandSource != null)
				{
					commandSource.sendFailure(Component.translatable("mocap.commands.error.improper_name"));
					commandSource.sendFailure(Component.translatable("mocap.commands.error.improper_name.character"));
				}
				return false;
			}
		}

		return true;
	}
}
