package net.mt1006.mocap.mocap.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.command.CommandSuggestions;
import net.mt1006.mocap.command.CommandUtils;
import net.mt1006.mocap.command.io.CommandOutput;
import net.mt1006.mocap.command.io.FullCommandInfo;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SceneFiles
{
	public static final int VERSION = MocapMod.SCENE_FORMAT_VERSION;

	public static boolean add(CommandOutput out, String name)
	{
		File file = Files.getSceneFile(out, name);
		if (file == null) { return false; }
		if (file.exists())
		{
			out.sendFailure("scenes.add.already_exists");
			return false;
		}

		SceneData sceneData = SceneData.empty();
		boolean success = sceneData.save(out, file, name, "scenes.add.success", "scenes.add.error");
		if (success) { CommandSuggestions.inputSet.add(nameWithDot(name)); }
		return success;
	}

	public static boolean copy(CommandOutput out, String srcName, String destName)
	{
		File srcFile = Files.getSceneFile(out, srcName);
		if (srcFile == null) { return false; }

		File destFile = Files.getSceneFile(out, destName);
		if (destFile == null) { return false; }

		try
		{
			FileUtils.copyFile(srcFile, destFile);
		}
		catch (IOException e) { return out.sendException(e, "scenes.copy.failed"); }

		CommandSuggestions.inputSet.add(nameWithDot(destName));
		List<String> elementCache = CommandSuggestions.sceneElementCache.get(nameWithDot(srcName));
		if (elementCache != null)
		{
			CommandSuggestions.sceneElementCache.put(nameWithDot(destName), new ArrayList<>(elementCache));
		}

		return out.sendSuccess("scenes.copy.success");
	}

	public static boolean rename(CommandOutput out, String oldName, String newName)
	{
		File oldFile = Files.getSceneFile(out, oldName);
		if (oldFile == null) { return false; }

		File newFile = Files.getSceneFile(out, newName);
		if (newFile == null) { return false; }

		if (!oldFile.renameTo(newFile))
		{
			return out.sendFailure("scenes.rename.failed");
		}

		CommandSuggestions.inputSet.remove(nameWithDot(oldName));
		CommandSuggestions.inputSet.add(nameWithDot(newName));
		List<String> elementCache = CommandSuggestions.sceneElementCache.get(nameWithDot(oldName));
		if (elementCache != null)
		{
			CommandSuggestions.sceneElementCache.remove(nameWithDot(oldName));
			CommandSuggestions.sceneElementCache.put(nameWithDot(newName), elementCache);
		}

		return out.sendSuccess("scenes.rename.success");
	}

	public static boolean remove(CommandOutput out, String name)
	{
		File sceneFile = Files.getSceneFile(out, name);
		if (sceneFile == null) { return false; }

		if (!sceneFile.delete())
		{
			out.sendFailure("scenes.remove.failed");
			return false;
		}

		CommandSuggestions.inputSet.remove(nameWithDot(name));
		CommandSuggestions.sceneElementCache.remove(nameWithDot(name));
		return out.sendSuccess("scenes.remove.success");
	}

	public static boolean addElement(CommandOutput out, String name, SceneData.Subscene subscene)
	{
		File file = Files.getSceneFile(out, name);
		SceneData sceneData = loadSceneData(out, file);
		if (sceneData == null) { return false; }

		sceneData.subscenes.add(subscene);
		return sceneData.save(out, file, name, "scenes.add_to.success", "scenes.add_to.error");
	}

	public static boolean removeElement(CommandOutput out, String name, String posStr)
	{
		Pair<Integer, String> posPair = CommandUtils.splitPosStr(posStr);
		int pos = posPair.getFirst();
		String expectedName = posPair.getSecond();

		File file = Files.getSceneFile(out, name);
		SceneData sceneData = loadSceneData(out, file);
		SceneData.Subscene subscene = SceneData.loadSubscene(out, sceneData, pos, expectedName);
		if (subscene == null) { return false; }

		sceneData.subscenes.remove(pos - 1);
		return sceneData.save(out, file, name, "scenes.remove_from.success", "scenes.remove_from.error");
	}

	public static boolean modify(FullCommandInfo info, String name, int pos, @Nullable String expectedName)
	{
		File file = Files.getSceneFile(info, name);
		SceneData sceneData = loadSceneData(info, file);
		SceneData.Subscene subscene = SceneData.loadSubscene(info, sceneData, pos, expectedName);
		if (subscene == null) { return false; }

		SceneData.Subscene newSubscene = modifySubscene(info, subscene);
		if (newSubscene == null) { return info.sendFailure("scenes.modify.error"); }

		sceneData.subscenes.set(pos - 1, newSubscene);
		return sceneData.save(info, file, name, "scenes.modify.success", "scenes.modify.error");
	}

	private static @Nullable SceneData.Subscene modifySubscene(FullCommandInfo rootCommandInfo, SceneData.Subscene oldSubscene)
	{
		SceneData.Subscene subscene = oldSubscene.copy();

		FullCommandInfo info = rootCommandInfo.getFinalCommandInfo();
		if (info == null)
		{
			rootCommandInfo.sendFailure("error.unable_to_get_argument");
			return null;
		}

		String propertyName = info.getNode(5);
		if (propertyName == null)
		{
			rootCommandInfo.sendFailure("error.unable_to_get_argument");
			return null;
		}

		try
		{
			if (propertyName.equals("subscene_name"))
			{
				subscene.name = info.getString("new_name");
				return subscene;
			}

			if (!subscene.modifiers.modify(info, propertyName, 5))
			{
				rootCommandInfo.sendFailure("error.generic");
				return null;
			}
			return subscene;
		}
		catch (Exception e)
		{
			rootCommandInfo.sendException(e, "error.unable_to_get_argument");
			return null;
		}
	}

	public static boolean elementInfo(CommandOutput out, String name, String posStr)
	{
		Pair<Integer, String> posPair = CommandUtils.splitPosStr(posStr);
		int pos = posPair.getFirst();
		String expectedName = posPair.getSecond();

		SceneData sceneData = loadSceneData(out, name);
		SceneData.Subscene subscene = SceneData.loadSubscene(out, sceneData, pos, expectedName);
		if (subscene == null) { return false; }

		out.sendSuccess("scenes.element_info.info");
		out.sendSuccess("scenes.element_info.id", name, pos);
		out.sendSuccess("scenes.element_info.name", subscene.name);

		subscene.modifiers.list(out);
		return true;
	}


	public static boolean listElements(CommandOutput out, String name)
	{
		SceneData sceneData = loadSceneData(out, name);
		if (sceneData == null) { return false; }

		out.sendSuccess("scenes.list_elements");

		int i = 1;
		for (SceneData.Subscene element : sceneData.subscenes)
		{
			out.sendSuccessLiteral("[%d] %s <%.3f> (%s)", i++, element.name,
					element.modifiers.startDelay.seconds, element.modifiers.playerName);
		}

		return out.sendSuccessLiteral("[id] name <start_delay> (player_name)");
	}

	public static boolean info(CommandOutput out, String name)
	{
		SceneData sceneData = new SceneData();
		if (!sceneData.load(out, name) && sceneData.version <= VERSION)
		{
			out.sendFailure("scenes.info.failed");
			return false;
		}

		out.sendSuccess("scenes.info.info");
		out.sendSuccess("file.info.name", name);
		if (!Files.printVersionInfo(out, VERSION, sceneData.version, sceneData.experimentalVersion)) { return true; }

		return out.sendSuccess("scenes.info.size", String.format("%.2f", sceneData.fileSize / 1024.0), sceneData.subscenes.size());
	}

	public static @Nullable List<String> list()
	{
		if (!Files.initialized) { return null; }

		String[] fileList = Files.sceneDirectory.list(Files::isSceneFile);
		if (fileList == null) { return null; }

		List<String> scenes = new ArrayList<>();
		for (String filename : fileList)
		{
			scenes.add("." + filename.substring(0, filename.lastIndexOf('.')));
		}

		Collections.sort(scenes);
		return scenes;
	}

	private static String nameWithDot(String name)
	{
		return name.charAt(0) == '.' ? name : ("." + name);
	}

	public static @Nullable SceneData loadSceneData(CommandOutput out, String name)
	{
		return loadSceneData(out, Files.getSceneFile(out, name));
	}

	public static @Nullable SceneData loadSceneData(CommandOutput out, @Nullable File file)
	{
		if (file == null) { return null; }
		if (!file.exists())
		{
			out.sendFailure("scenes.failure.file_not_exists");
			return null;
		}

		SceneData sceneData = new SceneData();
		return sceneData.load(out, file) ? sceneData : null;
	}

	public record Writer(JsonObject json)
	{
		public Writer()
		{
			this(new JsonObject());
		}

		public void addDouble(String name, double val, double def)
		{
			if (val != def) { json.add(name, new JsonPrimitive(val)); }
		}

		public void addBoolean(String name, boolean val, boolean def)
		{
			if (val != def) { json.add(name, new JsonPrimitive(val)); }
		}

		public void addString(String name, @Nullable String val)
		{
			if (val != null) { json.add(name, new JsonPrimitive(val)); }
		}

		public void addObject(String name, @Nullable Writer object)
		{
			if (object != null) { json.add(name, object.json); }
		}

		public void addVec3(String name, @Nullable Vec3 vec)
		{
			if (vec != null)
			{
				JsonArray array = new JsonArray();
				array.add(new JsonPrimitive(vec.x));
				array.add(new JsonPrimitive(vec.y));
				array.add(new JsonPrimitive(vec.z));
				json.add(name, array);
			}
		}

		public <T extends Enum<T>> void addEnum(String name, T val, T def)
		{
			if (val != def) { json.add(name, new JsonPrimitive(val.name().toLowerCase())); }
		}
	}

	public record Reader(JsonObject json)
	{
		public double readDouble(String name, double def)
		{
			JsonElement element = json.get(name);
			return element != null ? element.getAsDouble() : def;
		}

		public boolean readBoolean(String name, boolean def)
		{
			JsonElement element = json.get(name);
			return element != null ? element.getAsBoolean() : def;
		}

		public @Nullable String readString(String name)
		{
			JsonElement element = json.get(name);
			return element != null ? element.getAsString() : null;
		}

		public @Nullable Reader readObject(String name)
		{
			JsonElement element = json.get(name);
			return element != null ? new Reader(element.getAsJsonObject()) : null;
		}

		public @Nullable Vec3 readVec3(String name)
		{
			JsonElement element = json.get(name);
			if (element == null) { return null; }

			JsonArray array = element.getAsJsonArray();
			return new Vec3(array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble());
		}

		public <T extends Enum<T>> T readEnum(String name, T def)
		{
			JsonElement element = json.get(name);
			return element != null ? Enum.valueOf(def.getDeclaringClass(), element.getAsString().toUpperCase()) : def;
		}
	}
}
