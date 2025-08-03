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
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SceneFile extends Playable implements MocapSceneFile
{
	private final String name;
	private final File file;

	private SceneFile(String nameWithoutDot, File file)
	{
		this.name = "." + nameWithoutDot;
		this.file = file;
	}

	public static @Nullable SceneFile get(CommandOutput out, String name)
	{
		if (name.startsWith(".")) { name = name.substring(1); }
		return Files.check(out, name)
				? new SceneFile(name, new File(Files.sceneDirectory, name + Files.SCENE_EXTENSION))
				: null;
	}

	@Override public @Nullable MocapSceneFile copy(CommandOutput out, MocapSceneFile destFile)
	{
		try
		{
			FileUtils.copyFile(file, destFile.getFile());
		}
		catch (IOException e)
		{
			out.sendException(e, "scenes.copy.failed");
			return null;
		}

		CommandSuggestions.inputSet.add(destFile.getName());
		List<String> elementCache = CommandSuggestions.sceneElementCache.get(destFile.getName());
		if (elementCache != null)
		{
			CommandSuggestions.sceneElementCache.put(destFile.getName(), new ArrayList<>(elementCache));
		}

		out.sendSuccess("scenes.copy.success");
		return destFile;
	}

	@Override public @Nullable MocapSceneFile rename(CommandOutput out, MocapSceneFile destFile)
	{
		if (!file.renameTo(destFile.getFile()))
		{
			out.sendFailure("scenes.rename.failed");
			return null;
		}

		CommandSuggestions.inputSet.remove(name);
		CommandSuggestions.inputSet.add(destFile.getName());
		List<String> elementCache = CommandSuggestions.sceneElementCache.get(name);
		if (elementCache != null)
		{
			CommandSuggestions.sceneElementCache.remove(name);
			CommandSuggestions.sceneElementCache.put(destFile.getName(), elementCache);
		}

		out.sendSuccess("scenes.rename.success");
		return destFile;
	}

	@Override public boolean remove(CommandOutput out)
	{
		if (!file.delete()) { return out.sendFailure("scenes.remove.failed"); }

		CommandSuggestions.inputSet.remove(name);
		CommandSuggestions.sceneElementCache.remove(name);
		return out.sendSuccess("scenes.remove.success");
	}

	@Override public File getFile()
	{
		return file;
	}

	@Override public String getName()
	{
		return name;
	}

	@Override public boolean exists()
	{
		return file.exists();
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

		ScenePlayback playback = ScenePlayback.start(info, true, dataManager, dataManager.getScene(this), config, modifiers.getPlaybackModifiers(), null);
		return playback != null ? PlaybackManager.addPlayback(this, playback, isHidden) : null;
	}

	@Override public @Nullable Playback startAsSubscene(CommandInfo info, MocapModifiers modifiers, MocapPlaybackConfig config,
														PlaybackDataManager dataManager, PositionTransformer parentTransformer)
	{
		return ScenePlayback.start(info, false, dataManager, dataManager.getScene(this), config, modifiers.getPlaybackModifiers(), parentTransformer);
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
