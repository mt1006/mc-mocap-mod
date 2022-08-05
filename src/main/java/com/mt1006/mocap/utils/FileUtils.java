package com.mt1006.mocap.utils;

import com.mt1006.mocap.mocap.SceneInfo;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.storage.FolderName;

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

	private static boolean directoriesInitialized = false;
	private static File mocapDirectory = null;
	private static File recordingsDirectory = null;
	private static File scenesDirectory = null;

	public static boolean saveRecording(CommandSource commandSource, String name, ArrayList<Byte> recording)
	{
		if (!initDirectories(commandSource) || !checkIfProperName(name, commandSource)) { return false; }

		File recordingFile = new File(recordingsDirectory, name + RECORDINGS_EXTENSION);

		try
		{
			if (recordingFile.exists())
			{
				commandSource.sendFailure(new TranslationTextComponent("mocap.commands.recording.save.file_already_exist"));
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
			commandSource.sendFailure(new TranslationTextComponent("mocap.commands.recording.save.exception"));
			return false;
		}

		commandSource.sendSuccess(new TranslationTextComponent("mocap.commands.recording.save.success"), false);
		return true;
	}

	public static boolean removeRecording(CommandSource commandSource, String name)
	{
		if (!initDirectories(commandSource) || !checkIfProperName(name, commandSource)) { return false; }

		File recordingFile = new File(recordingsDirectory, name + RECORDINGS_EXTENSION);

		if (!recordingFile.delete())
		{
			commandSource.sendFailure(new TranslationTextComponent("mocap.commands.recording.remove.failed"));
			return false;
		}

		commandSource.sendSuccess(new TranslationTextComponent("mocap.commands.recording.remove.success"), false);
		return true;
	}

	@Nullable
	public static byte[] loadRecording(CommandSource commandSource, String name)
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
	public static ArrayList<String> recordingsList(CommandSource commandSource)
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

	public static boolean addScene(CommandSource commandSource, String name)
	{
		if (!initDirectories(commandSource) || !checkIfProperName(name, commandSource)) { return false; }

		File sceneFile = new File(scenesDirectory, name + SCENES_EXTENSION);

		try
		{
			if (sceneFile.exists())
			{
				commandSource.sendFailure(new TranslationTextComponent("mocap.commands.scenes.add.file_already_exists"));
				return false;
			}

			PrintWriter printWriter = new PrintWriter(sceneFile);
			printWriter.print(String.format("%d", SCENES_VERSION));
			printWriter.close();
		}
		catch (IOException exception)
		{
			commandSource.sendFailure(new TranslationTextComponent("mocap.commands.scenes.add.exception"));
			return false;
		}

		commandSource.sendSuccess(new TranslationTextComponent("mocap.commands.scenes.add.success"), false);
		return true;
	}

	public static boolean removeScene(CommandSource commandSource, String name)
	{
		if (!initDirectories(commandSource) || !checkIfProperName(name, commandSource)) { return false; }

		File sceneFile = new File(scenesDirectory, name + SCENES_EXTENSION);

		if (!sceneFile.delete())
		{
			commandSource.sendFailure(new TranslationTextComponent("mocap.commands.scenes.remove.failed"));
			return false;
		}

		commandSource.sendSuccess(new TranslationTextComponent("mocap.commands.scenes.remove.success"), false);
		return true;
	}

	public static boolean addToScene(CommandSource commandSource, String name, String lineToAdd)
	{
		if (!initDirectories(commandSource) || !checkIfProperName(name, commandSource)) { return false; }

		File sceneFile = new File(scenesDirectory, name + SCENES_EXTENSION);

		try
		{
			if (!sceneFile.exists())
			{
				commandSource.sendFailure(new TranslationTextComponent("mocap.commands.scenes.add_to.file_not_exists"));
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
			commandSource.sendFailure(new TranslationTextComponent("mocap.commands.scenes.add_to.exception"));
			return false;
		}

		commandSource.sendSuccess(new TranslationTextComponent("mocap.commands.scenes.add_to.success"), false);
		return true;
	}

	public static boolean removeFromScene(CommandSource commandSource, String name, int pos)
	{
		if (!initDirectories(commandSource) || !checkIfProperName(name, commandSource)) { return false; }

		File sceneFile = new File(scenesDirectory, name + SCENES_EXTENSION);

		try
		{
			if (!sceneFile.exists())
			{
				commandSource.sendFailure(new TranslationTextComponent("mocap.commands.scenes.remove_from.file_not_exists"));
				return false;
			}

			SceneInfo sceneInfo = new SceneInfo();
			if (!sceneInfo.load(commandSource, name)) { return false; }

			if (sceneInfo.subscenes.size() < pos || pos < 1)
			{
				commandSource.sendFailure(new TranslationTextComponent("mocap.commands.scenes.remove_from.wrong_element_pos"));
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
			commandSource.sendFailure(new TranslationTextComponent("mocap.commands.scenes.remove_from.exception"));
			return false;
		}

		commandSource.sendSuccess(new TranslationTextComponent("mocap.commands.scenes.remove_from.success"), false);
		return true;
	}

	@Nullable
	public static byte[] loadScene(CommandSource commandSource, String name)
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
			commandSource.sendFailure(new TranslationTextComponent("mocap.commands.error.failed_to_load_scene"));
			return null;
		}

		return scene;
	}

	@Nullable
	public static ArrayList<String> scenesList(CommandSource commandSource)
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

	private static boolean initDirectories(CommandSource commandSource)
	{
		if (directoriesInitialized) { return true; }

		MinecraftServer server = commandSource.getServer();

		mocapDirectory = server.getWorldPath(new FolderName(MOCAP_DIR_NAME)).toFile();
		if (!mocapDirectory.exists()) { mocapDirectory.mkdir(); }

		recordingsDirectory = new File(mocapDirectory, RECORDINGS_DIR_NAME);
		if (!recordingsDirectory.exists()) { recordingsDirectory.mkdir(); }

		scenesDirectory = new File(mocapDirectory, SCENES_DIR_NAME);
		if (!scenesDirectory.exists()) { scenesDirectory.mkdir(); }

		if (!mocapDirectory.isDirectory() || !recordingsDirectory.isDirectory() || !scenesDirectory.isDirectory())
		{
			commandSource.sendFailure(new TranslationTextComponent("mocap.commands.error.failed_to_init_directories"));
			return false;
		}

		directoriesInitialized = true;
		return true;
	}

	private static boolean checkIfProperName(String name, @Nullable CommandSource commandSource)
	{
		if (name.length() < 1) { return false; }

		if (name.charAt(0) == '.')
		{
			commandSource.sendFailure(new TranslationTextComponent("mocap.commands.error.improper_name.dot_first"));
			return false;
		}

		for (char c : name.toCharArray())
		{
			if (!(c >= 'a' && c <= 'z') && !(c >= '0' && c <= '9') &&
					c != '_' && c != '-' && c != '.')
			{
				if (commandSource != null)
				{
					commandSource.sendFailure(new TranslationTextComponent("mocap.commands.error.improper_name"));
				}
				return false;
			}
		}

		return true;
	}
}
