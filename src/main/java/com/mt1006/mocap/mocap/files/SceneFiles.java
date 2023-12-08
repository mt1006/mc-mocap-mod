package com.mt1006.mocap.mocap.files;

import com.mt1006.mocap.command.CommandInfo;
import com.mt1006.mocap.command.CommandOutput;
import com.mt1006.mocap.command.InputArgument;
import com.mt1006.mocap.mocap.playing.SceneData;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.command.arguments.EntitySummonArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SceneFiles
{
	public static final int SCENE_VERSION = 3;

	public static boolean add(CommandInfo commandInfo, String name)
	{
		File sceneFile = Files.getSceneFile(commandInfo, name);
		if (sceneFile == null) { return false; }

		try
		{
			if (sceneFile.exists())
			{
				commandInfo.sendFailure("mocap.scenes.add.error");
				commandInfo.sendFailure("mocap.scenes.add.error.file_already_exists");
				return false;
			}

			PrintWriter printWriter = new PrintWriter(sceneFile);
			printWriter.print(String.format("%d", SCENE_VERSION));
			printWriter.close();
		}
		catch (IOException exception)
		{
			commandInfo.sendException(exception, "mocap.scenes.add.error");
			return false;
		}

		InputArgument.addServerInput(nameWithDot(name));
		commandInfo.sendSuccess("mocap.scenes.add.success");
		return true;
	}

	public static boolean copy(CommandInfo commandInfo, String srcName, String destName)
	{
		File srcFile = Files.getSceneFile(commandInfo, srcName);
		if (srcFile == null) { return false; }

		File destFile = Files.getSceneFile(commandInfo, destName);
		if (destFile == null) { return false; }

		try { FileUtils.copyFile(srcFile, destFile); }
		catch (IOException exception)
		{
			commandInfo.sendException(exception, "mocap.scenes.copy.failed");
			return false;
		}

		InputArgument.addServerInput(nameWithDot(destName));
		commandInfo.sendSuccess("mocap.scenes.copy.success");
		return true;
	}

	public static boolean rename(CommandInfo commandInfo, String oldName, String newName)
	{
		File oldFile = Files.getSceneFile(commandInfo, oldName);
		if (oldFile == null) { return false; }

		File newFile = Files.getSceneFile(commandInfo, newName);
		if (newFile == null) { return false; }

		if (!oldFile.renameTo(newFile))
		{
			commandInfo.sendFailure("mocap.scenes.rename.failed");
			return false;
		}

		InputArgument.removeServerInput(nameWithDot(oldName));
		InputArgument.addServerInput(nameWithDot(newName));
		commandInfo.sendSuccess("mocap.scenes.rename.success");
		return true;
	}

	public static boolean remove(CommandInfo commandInfo, String name)
	{
		File sceneFile = Files.getSceneFile(commandInfo, name);
		if (sceneFile == null) { return false; }

		if (!sceneFile.delete())
		{
			commandInfo.sendFailure("mocap.scenes.remove.failed");
			return false;
		}

		InputArgument.removeServerInput(nameWithDot(name));
		commandInfo.sendSuccess("mocap.scenes.remove.success");
		return true;
	}

	public static boolean addElement(CommandInfo commandInfo, String name, String lineToAdd)
	{
		File sceneFile = Files.getSceneFile(commandInfo, name);
		if (sceneFile == null) { return false; }

		try
		{
			if (!sceneFile.exists())
			{
				commandInfo.sendFailure("mocap.scenes.add_to.error");
				commandInfo.sendFailure("mocap.scenes.error.file_not_exists");
				return false;
			}

			SceneData sceneData = new SceneData();
			if (!sceneData.load(commandInfo, name)) { return false; }

			PrintWriter printWriter = new PrintWriter(sceneFile);
			printWriter.print(String.format("%d", SCENE_VERSION));
			for (SceneData.Subscene subscene : sceneData.subscenes)
			{
				printWriter.print("\n" + subscene.sceneToStr());
			}
			printWriter.print("\n" + lineToAdd);
			printWriter.close();
		}
		catch (IOException exception)
		{
			commandInfo.sendException(exception, "mocap.scenes.add_to.error");
			return false;
		}

		commandInfo.sendSuccess("mocap.scenes.add_to.success");
		return true;
	}

	public static boolean removeElement(CommandInfo commandInfo, String name, int pos)
	{
		File sceneFile = Files.getSceneFile(commandInfo, name);
		if (sceneFile == null) { return false; }

		try
		{
			if (!sceneFile.exists())
			{
				commandInfo.sendFailure("mocap.scenes.remove_from.error");
				commandInfo.sendFailure("mocap.scenes.error.file_not_exists");
				return false;
			}

			SceneData sceneData = new SceneData();
			if (!sceneData.load(commandInfo, name)) { return false; }

			if (sceneData.subscenes.size() < pos || pos < 1)
			{
				commandInfo.sendFailure("mocap.scenes.remove_from.error");
				commandInfo.sendFailure("mocap.scenes.error.wrong_element_pos");
				commandInfo.sendFailure("mocap.scenes.error.wrong_element_pos.tip");
				return false;
			}

			int i = 1;
			PrintWriter printWriter = new PrintWriter(sceneFile);
			printWriter.print(String.format("%d", SCENE_VERSION));
			for (SceneData.Subscene subscene : sceneData.subscenes)
			{
				if (i++ != pos) { printWriter.print("\n" + subscene.sceneToStr()); }
			}
			printWriter.close();
		}
		catch (IOException exception)
		{
			commandInfo.sendException(exception, "mocap.scenes.remove_from.error");
			return false;
		}

		commandInfo.sendSuccess("mocap.scenes.remove_from.success");
		return true;
	}

	public static boolean modify(CommandInfo commandInfo, String name, int pos)
	{
		File sceneFile = Files.getSceneFile(commandInfo, name);
		if (sceneFile == null) { return false; }

		SceneData.Subscene newSubscene = null;
		try
		{
			if (!sceneFile.exists())
			{
				commandInfo.sendFailure("mocap.scenes.modify.error");
				commandInfo.sendFailure("mocap.scenes.error.file_not_exists");
				return false;
			}

			SceneData sceneData = new SceneData();
			if (!sceneData.load(commandInfo, name)) { return false; }

			if (sceneData.subscenes.size() < pos || pos < 1)
			{
				commandInfo.sendFailure("mocap.scenes.modify.error");
				commandInfo.sendFailure("mocap.scenes.error.wrong_element_pos");
				commandInfo.sendFailure("mocap.scenes.error.wrong_element_pos.tip");
				return false;
			}

			int i = 1;
			PrintWriter printWriter = new PrintWriter(sceneFile);
			printWriter.print(String.format("%d", SCENE_VERSION));
			for (SceneData.Subscene subscene : sceneData.subscenes)
			{
				if (i++ == pos)
				{
					newSubscene = modifySubscene(commandInfo, subscene);
					if (newSubscene != null) { subscene = newSubscene; }
				}
				printWriter.print("\n" + subscene.sceneToStr());
			}
			printWriter.close();
		}
		catch (IOException exception)
		{
			commandInfo.sendException(exception, "mocap.scenes.modify.error");
			return false;
		}

		if (newSubscene != null) { commandInfo.sendSuccess("mocap.scenes.modify.success"); }
		else { commandInfo.sendFailure("mocap.scenes.modify.error"); }
		return newSubscene != null;
	}

	private static @Nullable SceneData.Subscene modifySubscene(CommandInfo rootCommandInfo, SceneData.Subscene oldSubscene)
	{
		SceneData.Subscene subscene = oldSubscene.copy();

		CommandInfo commandInfo = rootCommandInfo.getFinalCommandInfo();
		if (commandInfo == null)
		{
			rootCommandInfo.sendFailure("mocap.error.unable_to_get_argument");
			return null;
		}

		String propertyName = commandInfo.getNode(5);
		if (propertyName == null)
		{
			rootCommandInfo.sendFailure("mocap.error.unable_to_get_argument");
			return null;
		}

		try
		{
			switch (propertyName)
			{
				case "subsceneName":
					subscene.name = commandInfo.getString("newName");
					return subscene;

				case "startDelay":
					subscene.startDelay = commandInfo.getDouble("delay");
					return subscene;

				case "positionOffset":
					subscene.posOffset[0] = commandInfo.getDouble("offsetX");
					subscene.posOffset[1] = commandInfo.getDouble("offsetY");
					subscene.posOffset[2] = commandInfo.getDouble("offsetZ");
					return subscene;

				case "playerInfo":
					subscene.playerData = commandInfo.getPlayerData();
					return subscene;

				case "playerAsEntity":
					String playerAsEntityStr = commandInfo.getNode(6);
					if (playerAsEntityStr == null) { break; }

					if (playerAsEntityStr.equals("enabled"))
					{
						subscene.playerAsEntityID = EntitySummonArgument.getSummonableEntity(commandInfo.ctx, "entity").toString();
						return subscene;
					}
					else if (playerAsEntityStr.equals("disabled"))
					{
						subscene.playerAsEntityID = null;
						return subscene;
					}
					break;
			}

			rootCommandInfo.sendFailure("mocap.error.unable_to_get_argument");
			return null;
		}
		catch (Exception exception)
		{
			rootCommandInfo.sendException(exception, "mocap.error.unable_to_get_argument");
			return null;
		}
	}

	public static boolean elementInfo(CommandInfo commandInfo, String name, int pos)
	{
		File sceneFile = Files.getSceneFile(commandInfo, name);
		if (sceneFile == null) { return false; }

		if (!sceneFile.exists())
		{
			commandInfo.sendFailure("mocap.scenes.element_info.failed");
			commandInfo.sendFailure("mocap.scenes.error.file_not_exists");
			return false;
		}

		SceneData sceneData = new SceneData();
		if (!sceneData.load(commandInfo, name)) { return false; }

		if (sceneData.subscenes.size() < pos || pos < 1)
		{
			commandInfo.sendFailure("mocap.scenes.element_info.failed");
			commandInfo.sendFailure("mocap.scenes.error.wrong_element_pos");
			commandInfo.sendFailure("mocap.scenes.error.wrong_element_pos.tip");
			return false;
		}

		SceneData.Subscene subscene = sceneData.subscenes.get(pos - 1);

		commandInfo.sendSuccess("mocap.scenes.element_info.info");
		commandInfo.sendSuccess("mocap.scenes.element_info.id", name, pos);
		commandInfo.sendSuccess("mocap.scenes.element_info.name", subscene.name);

		if (subscene.playerData.name == null) { commandInfo.sendSuccess("mocap.scenes.element_info.player_name.default"); }
		else { commandInfo.sendSuccess("mocap.scenes.element_info.player_name.custom", subscene.playerData.name); }

		switch (subscene.playerData.skinSource)
		{
			case DEFAULT:
				commandInfo.sendSuccess("mocap.scenes.element_info.skin.default");
				break;

			case FROM_PLAYER:
				commandInfo.sendSuccess("mocap.scenes.element_info.skin.profile", subscene.playerData.skinPath);
				break;

			case FROM_FILE:
				commandInfo.sendSuccess("mocap.scenes.element_info.skin.file", subscene.playerData.skinPath);
				break;

			case FROM_MINESKIN:
				commandInfo.sendSuccess("mocap.scenes.element_info.skin.mineskin");
				ITextComponent urlComponent = Utils.getURLComponent(subscene.playerData.skinPath, "  (§n%s§r)", subscene.playerData.skinPath);
				commandInfo.sendSuccessComponent(urlComponent);
				break;
		}

		commandInfo.sendSuccess("mocap.scenes.element_info.start_delay", subscene.startDelay, (int)Math.round(subscene.startDelay * 20.0));
		commandInfo.sendSuccess("mocap.scenes.element_info.offset", subscene.posOffset[0], subscene.posOffset[1], subscene.posOffset[2]);

		if (subscene.playerAsEntityID == null) { commandInfo.sendSuccess("mocap.scenes.element_info.player_as_entity.disabled"); }
		else { commandInfo.sendSuccess("mocap.scenes.element_info.player_as_entity.enabled", subscene.playerAsEntityID); }
		return true;
	}

	public static boolean listElements(CommandInfo commandInfo, String name)
	{
		SceneData sceneData = new SceneData();
		if(!sceneData.load(commandInfo, name)) { return false; }

		commandInfo.sendSuccess("mocap.scenes.list_elements");

		int i = 1;
		for (SceneData.Subscene element : sceneData.subscenes)
		{
			commandInfo.sendSuccessLiteral("[%d] %s <%.3f> [%.3f; %.3f; %.3f] (%s)", i++, element.name,
					element.startDelay, element.posOffset[0], element.posOffset[1], element.posOffset[2], element.playerData.name);
		}

		commandInfo.sendSuccessLiteral("[id] name <startDelay> [x; y; z] (playerName)");
		return true;
	}

	public static boolean info(CommandInfo commandInfo, String name)
	{
		SceneData sceneData = new SceneData();

		if (!sceneData.load(commandInfo, name) && sceneData.version <= SCENE_VERSION)
		{
			commandInfo.sendFailure("mocap.scenes.info.failed");
			return false;
		}

		commandInfo.sendSuccess("mocap.scenes.info.info");
		commandInfo.sendSuccess("mocap.file.info.name", name);

		if (sceneData.version <= SCENE_VERSION)
		{
			if (sceneData.version == SCENE_VERSION) { commandInfo.sendSuccess("mocap.file.info.version.current", sceneData.version); }
			else if (sceneData.version > 0) { commandInfo.sendSuccess("mocap.file.info.version.old", sceneData.version); }
			else { commandInfo.sendSuccess("mocap.file.info.version.unknown", sceneData.version); }

			commandInfo.sendSuccess("mocap.scenes.info.size", String.format("%.2f", sceneData.fileSize / 1024.0), sceneData.subscenes.size());
		}
		else
		{
			commandInfo.sendSuccess("mocap.file.info.version.not_supported", sceneData.version);
		}
		return true;
	}

	public static @Nullable List<String> list(MinecraftServer server, CommandOutput commandOutput)
	{
		if (!Files.initDirectories(server, commandOutput)) { return null; }

		ArrayList<String> scenes = new ArrayList<>();

		String[] fileList = Files.sceneDirectory.list();
		if (fileList == null) { return null; }

		for (String filename : fileList)
		{
			if (Files.isSceneFile(filename))
			{
				scenes.add("." + filename.substring(0, filename.lastIndexOf('.')));
			}
		}
		return scenes;
	}

	private static String nameWithDot(String name)
	{
		return name.charAt(0) == '.' ? name : ("." + name);
	}
}
