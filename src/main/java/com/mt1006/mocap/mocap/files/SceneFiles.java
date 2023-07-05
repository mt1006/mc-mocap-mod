package com.mt1006.mocap.mocap.files;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mt1006.mocap.command.CommandUtils;
import com.mt1006.mocap.command.InputArgument;
import com.mt1006.mocap.mocap.playing.SceneData;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.command.CommandSource;
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

	public static boolean add(CommandSource commandSource, String name)
	{
		File sceneFile = Files.getSceneFile(commandSource, name);
		if (sceneFile == null) { return false; }

		try
		{
			if (sceneFile.exists())
			{
				Utils.sendFailure(commandSource, "mocap.scenes.add.error");
				Utils.sendFailure(commandSource, "mocap.scenes.add.error.file_already_exists");
				return false;
			}

			PrintWriter printWriter = new PrintWriter(sceneFile);
			printWriter.print(String.format("%d", SCENE_VERSION));
			printWriter.close();
		}
		catch (IOException exception)
		{
			Utils.sendException(exception, commandSource, "mocap.scenes.add.error");
			return false;
		}

		InputArgument.addServerInput(nameWithDot(name));
		Utils.sendSuccess(commandSource, "mocap.scenes.add.success");
		return true;
	}

	public static boolean copy(CommandSource commandSource, String srcName, String destName)
	{
		File srcFile = Files.getSceneFile(commandSource, srcName);
		if (srcFile == null) { return false; }

		File destFile = Files.getSceneFile(commandSource, destName);
		if (destFile == null) { return false; }

		try { FileUtils.copyFile(srcFile, destFile); }
		catch (IOException exception)
		{
			Utils.sendException(exception, commandSource, "mocap.scene.copy.failed");
			return false;
		}

		InputArgument.addServerInput(nameWithDot(destName));
		Utils.sendSuccess(commandSource, "mocap.scene.copy.success");
		return true;
	}

	public static boolean rename(CommandSource commandSource, String oldName, String newName)
	{
		File oldFile = Files.getSceneFile(commandSource, oldName);
		if (oldFile == null) { return false; }

		File newFile = Files.getSceneFile(commandSource, newName);
		if (newFile == null) { return false; }

		if (!oldFile.renameTo(newFile))
		{
			Utils.sendFailure(commandSource, "mocap.scene.rename.failed");
			return false;
		}

		InputArgument.removeServerInput(nameWithDot(oldName));
		InputArgument.addServerInput(nameWithDot(newName));
		Utils.sendSuccess(commandSource, "mocap.scene.rename.success");
		return true;
	}

	public static boolean remove(CommandSource commandSource, String name)
	{
		File sceneFile = Files.getSceneFile(commandSource, name);
		if (sceneFile == null) { return false; }

		if (!sceneFile.delete())
		{
			Utils.sendFailure(commandSource, "mocap.scenes.remove.failed");
			return false;
		}

		InputArgument.removeServerInput(nameWithDot(name));
		Utils.sendSuccess(commandSource, "mocap.scenes.remove.success");
		return true;
	}

	public static boolean addElement(CommandSource commandSource, String name, String lineToAdd)
	{
		File sceneFile = Files.getSceneFile(commandSource, name);
		if (sceneFile == null) { return false; }

		try
		{
			if (!sceneFile.exists())
			{
				Utils.sendFailure(commandSource, "mocap.scenes.add_to.error");
				Utils.sendFailure(commandSource, "mocap.scenes.error.file_not_exists");
				return false;
			}

			SceneData sceneData = new SceneData();
			if (!sceneData.load(commandSource, name)) { return false; }

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
			Utils.sendException(exception, commandSource, "mocap.scenes.add_to.error");
			return false;
		}

		Utils.sendSuccess(commandSource, "mocap.scenes.add_to.success");
		return true;
	}

	public static boolean removeElement(CommandSource commandSource, String name, int pos)
	{
		File sceneFile = Files.getSceneFile(commandSource, name);
		if (sceneFile == null) { return false; }

		try
		{
			if (!sceneFile.exists())
			{
				Utils.sendFailure(commandSource, "mocap.scenes.remove_from.error");
				Utils.sendFailure(commandSource, "mocap.scenes.error.file_not_exists");
				return false;
			}

			SceneData sceneData = new SceneData();
			if (!sceneData.load(commandSource, name)) { return false; }

			if (sceneData.subscenes.size() < pos || pos < 1)
			{
				Utils.sendFailure(commandSource, "mocap.scenes.remove_from.error");
				Utils.sendFailure(commandSource, "mocap.scenes.error.wrong_element_pos");
				Utils.sendFailure(commandSource, "mocap.scenes.error.wrong_element_pos.tip");
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
			Utils.sendException(exception, commandSource, "mocap.scenes.remove_from.error");
			return false;
		}

		Utils.sendSuccess(commandSource, "mocap.scenes.remove_from.success");
		return true;
	}

	public static boolean modify(CommandContext<CommandSource> ctx, String name, int pos)
	{
		CommandSource commandSource = ctx.getSource();

		File sceneFile = Files.getSceneFile(commandSource, name);
		if (sceneFile == null) { return false; }

		SceneData.Subscene newSubscene = null;
		try
		{
			if (!sceneFile.exists())
			{
				Utils.sendFailure(commandSource, "mocap.scenes.modify.error");
				Utils.sendFailure(commandSource, "mocap.scenes.error.file_not_exists");
				return false;
			}

			SceneData sceneData = new SceneData();
			if (!sceneData.load(commandSource, name)) { return false; }

			if (sceneData.subscenes.size() < pos || pos < 1)
			{
				Utils.sendFailure(commandSource, "mocap.scenes.modify.error");
				Utils.sendFailure(commandSource, "mocap.scenes.error.wrong_element_pos");
				Utils.sendFailure(commandSource, "mocap.scenes.error.wrong_element_pos.tip");
				return false;
			}

			int i = 1;
			PrintWriter printWriter = new PrintWriter(sceneFile);
			printWriter.print(String.format("%d", SCENE_VERSION));
			for (SceneData.Subscene subscene : sceneData.subscenes)
			{
				if (i++ == pos)
				{
					newSubscene = modifySubscene(ctx, subscene);
					if (newSubscene != null) { subscene = newSubscene; }
				}
				printWriter.print("\n" + subscene.sceneToStr());
			}
			printWriter.close();
		}
		catch (IOException exception)
		{
			Utils.sendException(exception, commandSource, "mocap.scenes.modify.error");
			return false;
		}

		if (newSubscene != null) { Utils.sendSuccess(commandSource, "mocap.scenes.modify.success"); }
		else { Utils.sendFailure(commandSource, "mocap.scenes.modify.error"); }
		return newSubscene != null;
	}

	private static @Nullable SceneData.Subscene modifySubscene(CommandContext<CommandSource> rootCtx, SceneData.Subscene oldSubscene)
	{
		CommandSource commandSource = rootCtx.getSource();
		SceneData.Subscene subscene = oldSubscene.copy();

		CommandContext<CommandSource> ctx = CommandUtils.getFinalCommandContext(rootCtx);
		if (ctx == null)
		{
			Utils.sendFailure(commandSource, "mocap.error.unable_to_get_argument");
			return null;
		}

		String propertyName = CommandUtils.getCommandNode(ctx, 5);
		if (propertyName == null)
		{
			Utils.sendFailure(commandSource, "mocap.error.unable_to_get_argument");
			return null;
		}

		try
		{
			switch (propertyName)
			{
				case "subsceneName":
					subscene.name = StringArgumentType.getString(ctx, "newName");
					return subscene;

				case "startDelay":
					subscene.startDelay = DoubleArgumentType.getDouble(ctx, "delay");
					return subscene;

				case "positionOffset":
					subscene.posOffset[0] = DoubleArgumentType.getDouble(ctx, "offsetX");
					subscene.posOffset[1] = DoubleArgumentType.getDouble(ctx, "offsetY");
					subscene.posOffset[2] = DoubleArgumentType.getDouble(ctx, "offsetZ");
					return subscene;

				case "playerInfo":
					subscene.playerData = CommandUtils.getPlayerData(ctx);
					return subscene;

				case "playerAsEntity":
					String playerAsEntityStr = CommandUtils.getCommandNode(ctx, 6);
					if (playerAsEntityStr == null) { break; }

					if (playerAsEntityStr.equals("enabled"))
					{
						subscene.playerAsEntityID = EntitySummonArgument.getSummonableEntity(ctx, "entity").toString();
						return subscene;
					}
					else if (playerAsEntityStr.equals("disabled"))
					{
						subscene.playerAsEntityID = null;
						return subscene;
					}
					break;
			}

			Utils.sendFailure(commandSource, "mocap.error.unable_to_get_argument");
			return null;
		}
		catch (Exception exception)
		{
			Utils.sendException(exception, commandSource, "mocap.error.unable_to_get_argument");
			return null;
		}
	}

	public static boolean elementInfo(CommandSource commandSource, String name, int pos)
	{
		File sceneFile = Files.getSceneFile(commandSource, name);
		if (sceneFile == null) { return false; }

		if (!sceneFile.exists())
		{
			Utils.sendFailure(commandSource, "mocap.scenes.element_info.failed");
			Utils.sendFailure(commandSource, "mocap.scenes.error.file_not_exists");
			return false;
		}

		SceneData sceneData = new SceneData();
		if (!sceneData.load(commandSource, name)) { return false; }

		if (sceneData.subscenes.size() < pos || pos < 1)
		{
			Utils.sendFailure(commandSource, "mocap.scenes.element_info.failed");
			Utils.sendFailure(commandSource, "mocap.scenes.error.wrong_element_pos");
			Utils.sendFailure(commandSource, "mocap.scenes.error.wrong_element_pos.tip");
			return false;
		}

		SceneData.Subscene subscene = sceneData.subscenes.get(pos - 1);

		Utils.sendSuccess(commandSource, "mocap.scenes.element_info.info");
		Utils.sendSuccess(commandSource, "mocap.scenes.element_info.id", name, pos);
		Utils.sendSuccess(commandSource, "mocap.scenes.element_info.name", subscene.name);

		if (subscene.playerData.name == null) { Utils.sendSuccess(commandSource, "mocap.scenes.element_info.player_name.default"); }
		else { Utils.sendSuccess(commandSource, "mocap.scenes.element_info.player_name.custom", subscene.playerData.name); }

		switch (subscene.playerData.skinSource)
		{
			case DEFAULT:
				Utils.sendSuccess(commandSource, "mocap.scenes.element_info.skin.default");
				break;

			case FROM_PLAYER:
				Utils.sendSuccess(commandSource, "mocap.scenes.element_info.skin.profile", subscene.playerData.skinPath);
				break;

			case FROM_FILE:
				Utils.sendSuccess(commandSource, "mocap.scenes.element_info.skin.file", subscene.playerData.skinPath);
				break;

			case FROM_MINESKIN:
				Utils.sendSuccess(commandSource, "mocap.scenes.element_info.skin.mineskin");
				ITextComponent urlComponent = Utils.getURLComponent(subscene.playerData.skinPath, "  (§n%s§r)", subscene.playerData.skinPath);
				Utils.sendSuccessComponent(commandSource, urlComponent);
				break;
		}

		Utils.sendSuccess(commandSource, "mocap.scenes.element_info.start_delay", subscene.startDelay, (int)Math.round(subscene.startDelay * 20.0));
		Utils.sendSuccess(commandSource, "mocap.scenes.element_info.offset", subscene.posOffset[0], subscene.posOffset[1], subscene.posOffset[2]);

		if (subscene.playerAsEntityID == null) { Utils.sendSuccess(commandSource, "mocap.scenes.element_info.player_as_entity.disabled"); }
		else { Utils.sendSuccess(commandSource, "mocap.scenes.element_info.player_as_entity.enabled", subscene.playerAsEntityID); }
		return true;
	}

	public static boolean listElements(CommandSource commandSource, String name)
	{
		SceneData sceneData = new SceneData();
		if(!sceneData.load(commandSource, name)) { return false; }

		Utils.sendSuccess(commandSource, "mocap.scenes.list_elements");

		int i = 1;
		for (SceneData.Subscene element : sceneData.subscenes)
		{
			Utils.sendSuccessLiteral(commandSource, "[%d] %s <%.3f> [%.3f; %.3f; %.3f] (%s)", i++, element.name,
					element.startDelay, element.posOffset[0], element.posOffset[1], element.posOffset[2], element.playerData.name);
		}

		Utils.sendSuccessLiteral(commandSource, "[id] name <startDelay> [x; y; z] (playerName)");
		return true;
	}

	public static boolean info(CommandSource commandSource, String name)
	{
		SceneData sceneData = new SceneData();

		if (!sceneData.load(commandSource, name) && sceneData.version <= SCENE_VERSION)
		{
			Utils.sendFailure(commandSource, "mocap.scenes.info.failed");
			return false;
		}

		Utils.sendSuccess(commandSource, "mocap.scenes.info.info");
		Utils.sendSuccess(commandSource, "mocap.file.info.name", name);

		if (sceneData.version <= SCENE_VERSION)
		{
			if (sceneData.version == SCENE_VERSION)
			{
				Utils.sendSuccess(commandSource, "mocap.file.info.version.current", sceneData.version);
			}
			else if (sceneData.version > 0)
			{
				Utils.sendSuccess(commandSource, "mocap.file.info.version.old", sceneData.version);
			}
			else
			{
				Utils.sendSuccess(commandSource, "mocap.file.info.version.unknown", sceneData.version);
			}

			Utils.sendSuccess(commandSource, "mocap.scenes.info.size",
					String.format("%.2f", sceneData.fileSize / 1024.0), sceneData.subscenes.size());
		}
		else
		{
			Utils.sendSuccess(commandSource, "mocap.file.info.version.not_supported", sceneData.version);
		}
		return true;
	}

	public static @Nullable List<String> list(MinecraftServer server, @Nullable CommandSource commandSource)
	{
		if (!Files.initDirectories(server, commandSource)) { return null; }

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
