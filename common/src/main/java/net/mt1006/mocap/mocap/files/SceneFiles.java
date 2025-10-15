package net.mt1006.mocap.mocap.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.v1.controller.playable.MocapSceneElement;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.command.CommandSuggestions;
import net.mt1006.mocap.command.CommandUtils;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.playing.playable.SceneFile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SceneFiles
{
	public static final int VERSION = MocapMod.SCENE_FORMAT_VERSION;

	public static boolean add(CommandOutput out, String name)
	{
		SceneFile file = SceneFile.get(out, name);
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

	public static boolean addElement(CommandOutput out, String name, MocapSceneElement element)
	{
		SceneFile file = SceneFile.get(out, name);
		return file != null && file.add(out, element);
	}

	public static boolean removeElement(CommandOutput out, String name, String posStr)
	{
		Pair<Integer, String> posPair = CommandUtils.splitPosStr(posStr);
		int pos = posPair.getFirst();
		String expectedName = posPair.getSecond();

		SceneFile file = SceneFile.get(out, name);
		SceneData sceneData = loadSceneData(out, file);
		MocapSceneElement element = SceneData.loadSubscene(out, sceneData, pos, expectedName);
		if (element == null) { return false; }

		sceneData.elements.remove(pos - 1);
		return sceneData.save(out, file, name, "scenes.remove_from.success", "scenes.remove_from.error");
	}

	public static boolean modify(FullCommandInfo info, String name, int pos, @Nullable String expectedName)
	{
		SceneFile file = SceneFile.get(info, name);
		SceneData sceneData = loadSceneData(info, file);
		MocapSceneElement element = SceneData.loadSubscene(info, sceneData, pos, expectedName);
		if (element == null) { return false; }

		MocapSceneElement newElement = modifySubscene(info, element);
		if (newElement == null) { return info.sendFailure("scenes.modify.error"); }

		sceneData.elements.set(pos - 1, newElement);
		return sceneData.save(info, file, name, "scenes.modify.success", "scenes.modify.error");
	}

	private static @Nullable MocapSceneElement modifySubscene(FullCommandInfo rootCommandInfo, MocapSceneElement element)
	{
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
				return element.withName(info.getString("new_name"));
			}
			else
			{
				MocapModifiers newModifiers = element.getModifiers().modify(info, propertyName, 5);
				if (newModifiers == null)
				{
					rootCommandInfo.sendFailure("error.generic");
					return null;
				}
				return element.withModifiers(newModifiers);
			}
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
		MocapSceneElement element = SceneData.loadSubscene(out, sceneData, pos, expectedName);
		if (element == null) { return false; }

		out.sendSuccess("scenes.element_info.info");
		out.sendSuccess("scenes.element_info.id", name, pos);
		out.sendSuccess("scenes.element_info.name", element.getName());

		element.getModifiers().list(out);
		return true;
	}


	public static boolean listElements(CommandOutput out, String name)
	{
		SceneData sceneData = loadSceneData(out, name);
		if (sceneData == null) { return false; }

		out.sendSuccess("scenes.list_elements");

		int i = 1;
		for (MocapSceneElement element : sceneData.elements)
		{
			out.sendSuccessLiteral("[%d] %s (%s)", i++, element.getName(), element.getModifiers().getPlayerName());
		}

		return out.sendSuccessLiteral("[id] name (player_name)");
	}

	public static boolean info(CommandOutput out, String name)
	{
		SceneData sceneData = new SceneData();
		if (!sceneData.load(out, SceneFile.get(out, name)) && sceneData.version <= VERSION)
		{
			out.sendFailure("scenes.info.failed");
			return false;
		}

		out.sendSuccess("scenes.info.info");
		out.sendSuccess("file.info.name", name);
		if (!Files.printVersionInfo(out, VERSION, sceneData.version, sceneData.experimentalVersion)) { return true; }

		return out.sendSuccess("scenes.info.size", String.format("%.2f", sceneData.fileSize / 1024.0), sceneData.elements.size());
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
		return loadSceneData(out, SceneFile.get(out, name));
	}

	public static @Nullable SceneData loadSceneData(CommandOutput out, @Nullable SceneFile file)
	{
		return file != null ? file.loadSceneData(out) : null;
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
