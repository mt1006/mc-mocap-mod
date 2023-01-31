package com.mt1006.mocap.mocap.files;

import com.mt1006.mocap.mocap.playing.SceneInfo;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SceneFile
{
	public static final int SCENE_VERSION = 2;

	public static boolean add(CommandSourceStack commandSource, String name)
	{
		File sceneFile = Files.getSceneFile(commandSource, name);
		if (sceneFile == null) { return false; }

		try
		{
			if (sceneFile.exists())
			{
				Utils.sendFailure(commandSource, "mocap.commands.scenes.add.error");
				Utils.sendFailure(commandSource, "mocap.commands.scenes.add.error.file_already_exists");
				return false;
			}

			PrintWriter printWriter = new PrintWriter(sceneFile);
			printWriter.print(String.format("%d", SCENE_VERSION));
			printWriter.close();
		}
		catch (IOException exception)
		{
			Utils.sendFailure(commandSource, "mocap.commands.scenes.add.error");
			return false;
		}

		Utils.sendSuccess(commandSource, "mocap.commands.scenes.add.success");
		return true;
	}

	public static boolean remove(CommandSourceStack commandSource, String name)
	{
		File sceneFile = Files.getSceneFile(commandSource, name);
		if (sceneFile == null) { return false; }

		if (!sceneFile.delete())
		{
			Utils.sendFailure(commandSource, "mocap.commands.scenes.remove.failed");
			return false;
		}

		Utils.sendSuccess(commandSource, "mocap.commands.scenes.remove.success");
		return true;
	}

	public static boolean addElement(CommandSourceStack commandSource, String name, String lineToAdd)
	{
		File sceneFile = Files.getSceneFile(commandSource, name);
		if (sceneFile == null) { return false; }

		try
		{
			if (!sceneFile.exists())
			{
				Utils.sendFailure(commandSource, "mocap.commands.scenes.add_to.error");
				Utils.sendFailure(commandSource, "mocap.commands.scenes.add_to.error.file_not_exists");
				return false;
			}

			SceneInfo sceneInfo = new SceneInfo();
			if (!sceneInfo.load(commandSource, name)) { return false; }

			PrintWriter printWriter = new PrintWriter(sceneFile);
			printWriter.print(String.format("%d", SCENE_VERSION));
			for (SceneInfo.Subscene subscene : sceneInfo.subscenes)
			{
				printWriter.print("\n" + subscene.sceneToStr());
			}
			printWriter.print("\n" + lineToAdd);
			printWriter.close();
		}
		catch (IOException exception)
		{
			Utils.sendFailure(commandSource, "mocap.commands.scenes.add_to.error");
			return false;
		}

		Utils.sendSuccess(commandSource, "mocap.commands.scenes.add_to.success");
		return true;
	}

	public static boolean removeElement(CommandSourceStack commandSource, String name, int pos)
	{
		File sceneFile = Files.getSceneFile(commandSource, name);
		if (sceneFile == null) { return false; }

		try
		{
			if (!sceneFile.exists())
			{
				Utils.sendFailure(commandSource, "mocap.commands.scenes.remove_from.error");
				Utils.sendFailure(commandSource, "mocap.commands.scenes.remove_from.error.file_not_exists");
				return false;
			}

			SceneInfo sceneInfo = new SceneInfo();
			if (!sceneInfo.load(commandSource, name)) { return false; }

			if (sceneInfo.subscenes.size() < pos || pos < 1)
			{
				Utils.sendFailure(commandSource, "mocap.commands.scenes.remove_from.error");
				Utils.sendFailure(commandSource, "mocap.commands.scenes.remove_from.error.wrong_element_pos");
				Utils.sendFailure(commandSource, "mocap.commands.scenes.remove_from.error.wrong_element_pos.tip");
				return false;
			}

			int i = 1;
			PrintWriter printWriter = new PrintWriter(sceneFile);
			printWriter.print(String.format("%d", SCENE_VERSION));
			for (SceneInfo.Subscene subscene : sceneInfo.subscenes)
			{
				if (i++ != pos) { printWriter.print("\n" + subscene.sceneToStr()); }
			}
			printWriter.close();
		}
		catch (IOException exception)
		{
			Utils.sendFailure(commandSource, "mocap.commands.scenes.remove_from.error");
			return false;
		}

		Utils.sendSuccess(commandSource, "mocap.commands.scenes.remove_from.success");
		return true;
	}

	public static @Nullable List<String> list(CommandSourceStack commandSource)
	{
		if (!Files.initDirectories(commandSource)) { return null; }

		ArrayList<String> scenes = new ArrayList<>();

		String[] fileList = Files.scenesDirectory.list();
		if (fileList == null) { return null; }

		for (String filename : fileList)
		{
			if (Files.isSceneFile(filename))
			{
				scenes.add(filename.substring(0, filename.lastIndexOf('.')));
			}
		}
		return scenes;
	}
}
