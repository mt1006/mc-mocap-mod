package net.mt1006.mocap.mocap.playing.playable;

import net.mt1006.mocap.api.v1.controller.MocapPlaybackRoot;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.controller.playable.MocapSceneElement;
import net.mt1006.mocap.api.v1.controller.playable.MocapSceneFile;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.command.CommandSuggestions;
import net.mt1006.mocap.mocap.files.Files;
import net.mt1006.mocap.mocap.files.SceneData;
import net.mt1006.mocap.mocap.playing.PlaybackDataManager;
import net.mt1006.mocap.mocap.playing.PlaybackManager;
import net.mt1006.mocap.mocap.playing.playback.Playback;
import net.mt1006.mocap.mocap.playing.playback.PositionTransformer;
import net.mt1006.mocap.mocap.playing.playback.ScenePlayback;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SceneFile extends PlayableFile<MocapSceneFile> implements MocapSceneFile
{
	public static @Nullable SceneFile get(CommandOutput out, String name)
	{
		if (name.startsWith(".")) { name = name.substring(1); }
		return Files.check(out, name)
				? new SceneFile(name, new File(Files.sceneDirectory, name + Files.SCENE_EXTENSION))
				: null;
	}

	private SceneFile(String nameWithoutDot, File file)
	{
		super("." + nameWithoutDot, file);
	}

	@Override public @Nullable MocapSceneFile copy(CommandOutput out, MocapSceneFile destFile)
	{
		if (super.copy(out, destFile) == null) { return null; }

		List<String> elementCache = CommandSuggestions.sceneElementCache.get(name);
		if (elementCache != null)
		{
			CommandSuggestions.sceneElementCache.put(destFile.getName(), new ArrayList<>(elementCache));
		}
		return destFile;
	}

	@Override public @Nullable MocapSceneFile rename(CommandOutput out, MocapSceneFile destFile)
	{
		if (super.rename(out, destFile) == null) { return null; }

		List<String> elementCache = CommandSuggestions.sceneElementCache.get(name);
		if (elementCache != null)
		{
			CommandSuggestions.sceneElementCache.remove(name);
			CommandSuggestions.sceneElementCache.put(destFile.getName(), elementCache);
		}
		return destFile;
	}

	@Override public boolean remove(CommandOutput out)
	{
		if (!super.remove(out)) { return false; }

		CommandSuggestions.sceneElementCache.remove(name);
		return true;
	}

	@Override protected String getTextComponentKey(String key)
	{
		return "scenes." + key;
	}

	@Override public boolean add(CommandOutput out, MocapSceneElement element)
	{
		SceneData data = loadSceneData(out);
		if (data == null) { return false; }

		data.elements.add(element);
		return data.save(out, this, name, "scenes.add_to.success", "scenes.add_to.error");
	}

	@Override public @Nullable List<? extends MocapSceneElement> getAll(CommandOutput out)
	{
		SceneData data = loadSceneData(out);
		return data != null ? Collections.unmodifiableList(data.elements) : null;
	}

	@Override public boolean replaceAll(CommandOutput out, List<? extends MocapSceneElement> elements)
	{
		SceneData data = loadSceneData(out);
		if (data == null) { return false; }

		data.elements.clear();
		data.elements.addAll(elements);
		return data.save(out, this, name, "scenes.modify.success", "scenes.modify.error");
	}

	@Override public boolean clear(CommandOutput out)
	{
		SceneData data = loadSceneData(out);
		if (data == null) { return false; }

		data.elements.clear();
		return data.save(out, this, name, "scenes.modify.success", "scenes.modify.error");
	}

	@Override public @Nullable MocapPlaybackRoot startPlayback(CommandInfo info, MocapModifiers modifiers, MocapPlaybackConfig config, boolean isHidden)
	{
		PlaybackDataManager dataManager = new PlaybackDataManager();
		dataManager.loadScene(info, this);

		ScenePlayback playback = ScenePlayback.start(info, true, dataManager, dataManager.getScene(this), config, modifiers, null);
		return playback != null ? PlaybackManager.onStart(this, playback, isHidden) : null;
	}

	@Override public @Nullable Playback startAsSubscene(CommandInfo info, MocapModifiers modifiers, MocapPlaybackConfig config,
														PlaybackDataManager dataManager, PositionTransformer parentTransformer)
	{
		return ScenePlayback.start(info, false, dataManager, dataManager.getScene(this), config, modifiers, parentTransformer);
	}

	public @Nullable SceneData loadSceneData(CommandOutput out)
	{
		if (!file.exists())
		{
			out.sendFailure("scenes.failure.file_not_exists");
			return null;
		}

		SceneData sceneData = new SceneData();
		return sceneData.load(out, this) ? sceneData : null;
	}
}
