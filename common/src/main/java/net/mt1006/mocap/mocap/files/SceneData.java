package net.mt1006.mocap.mocap.files;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.command.CommandSuggestions;
import net.mt1006.mocap.command.io.CommandOutput;
import net.mt1006.mocap.mocap.playing.modifiers.PlaybackModifiers;
import net.mt1006.mocap.mocap.settings.Settings;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SceneData
{
	public final List<Subscene> subscenes = new ArrayList<>();
	public int version = 0;
	public boolean experimentalVersion = false;
	public long fileSize = 0;

	public static SceneData empty()
	{
		SceneData sceneData = new SceneData();
		sceneData.version = SceneFiles.VERSION;
		sceneData.experimentalVersion = MocapMod.EXPERIMENTAL;
		return sceneData;
	}

	public boolean save(CommandOutput out, File file, String sceneName, String onSuccess, String onError)
	{
		JsonObject json = new JsonObject();
		json.add("version", new JsonPrimitive(experimentalVersion ? (-version) : version)); //TODO: fix?

		JsonArray subscenesArray = new JsonArray();
		subscenes.forEach((s) -> subscenesArray.add(s.toJson()));
		json.add("subscenes", subscenesArray);

		try
		{
			FileWriter writer = new FileWriter(file);
			GsonBuilder gsonBuilder = Settings.PRETTY_SCENE_FILES.val ? new GsonBuilder().setPrettyPrinting() : new GsonBuilder();
			gsonBuilder.create().toJson(json, writer);
			writer.close();

			saveToSceneElementCache(sceneName);
			out.sendSuccess(onSuccess);
			return true;
		}
		catch (Exception e)
		{
			out.sendException(e, onError);
			return false;
		}
	}

	public boolean load(CommandOutput out, String name)
	{
		return load(out, Files.getSceneFile(out, name));
	}

	public boolean load(CommandOutput out, File file)
	{
		byte[] data = Files.loadFile(file);
		return data != null && load(out, data);
	}

	private boolean load(CommandOutput out, byte[] scene)
	{
		fileSize = scene.length;

		LegacySceneDataParser legacyParser = new LegacySceneDataParser(this, out, scene);
		if (legacyParser.isLegacy()) { return legacyParser.wasParsed(); }

		try
		{
			JsonElement jsonElement = new JsonParser().parse(new InputStreamReader(new ByteArrayInputStream(scene)));
			JsonObject json = jsonElement.getAsJsonObject();
			if (json == null) { throw new Exception("Scene file isn't a JSON object!"); }

			JsonElement versionElement = json.get("version");
			if (versionElement == null) { throw new Exception("Scene version not specified!"); }
			if (!setAndVerifyVersion(out, versionElement.getAsInt())) { return false; }

			JsonElement subsceneArrayElement = json.get("subscenes");
			if (subsceneArrayElement == null) { throw new Exception("Scene subscenes list not found!"); }

			for (JsonElement subsceneElement : subsceneArrayElement.getAsJsonArray())
			{
				JsonObject subsceneObject = subsceneElement.getAsJsonObject();
				if (subsceneObject == null) { throw new Exception("Scene subscene isn't a JSON object!"); }
				subscenes.add(new Subscene(subsceneObject));
			}
			return true;
		}
		catch (Exception e) { return out.sendException(e, "error.failed_to_load_scene"); }
	}

	public boolean setAndVerifyVersion(CommandOutput out, int versionNumber)
	{
		version = Math.abs(versionNumber);
		experimentalVersion = (versionNumber < 0);

		if (version > SceneFiles.VERSION)
		{
			out.sendFailure("error.failed_to_load_scene");
			out.sendFailure("error.failed_to_load_scene.not_supported");
			return false;
		}
		return true;
	}

	public @Nullable List<String> saveToSceneElementCache(String sceneName)
	{
		List<String> elements = new ArrayList<>(subscenes.size());
		int id = 1;
		for (SceneData.Subscene subscene : subscenes)
		{
			elements.add(String.format("%03d-%s", id, subscene.name));
			id++;
		}

		CommandSuggestions.sceneElementCache.put(sceneName, elements);
		return elements;
	}

	public static @Nullable SceneData.Subscene loadSubscene(CommandOutput out, @Nullable SceneData sceneData,
															Pair<Integer, @Nullable String> pair)
	{
		return loadSubscene(out, sceneData, pair.getFirst(), pair.getSecond());
	}

	public static @Nullable SceneData.Subscene loadSubscene(CommandOutput out, @Nullable SceneData sceneData,
															int pos, @Nullable String expectedName)
	{
		if (sceneData == null) { return null; }

		if (sceneData.subscenes.size() < pos || pos < 1)
		{
			out.sendFailureWithTip("scenes.failure.wrong_element_pos");
			return null;
		}

		SceneData.Subscene subscene = sceneData.subscenes.get(pos - 1);
		if (expectedName != null && !expectedName.equals(subscene.name))
		{
			out.sendFailure("scenes.failure.wrong_subscene_name");
			return null;
		}
		return subscene;
	}

	public static class Subscene
	{
		public String name;
		public PlaybackModifiers modifiers;

		public Subscene(String name, PlaybackModifiers modifiers)
		{
			this.name = name;
			this.modifiers = modifiers;
		}

		public Subscene(JsonObject json) throws Exception
		{
			JsonElement nameElement = json.get("name");
			if (nameElement == null) { throw new Exception("JSON \"name\" element not found!"); }

			name = nameElement.getAsString();
			modifiers = new PlaybackModifiers(new SceneFiles.Reader(json));
		}

		public JsonObject toJson()
		{
			JsonObject json = new JsonObject();
			json.add("name", new JsonPrimitive(name));
			modifiers.save(new SceneFiles.Writer(json));
			return json;
		}

		public Subscene copy()
		{
			try
			{
				return new Subscene(toJson());
			}
			catch (Exception e) { throw new RuntimeException("Something went wrong when copying subscene!"); }
		}
	}
}
