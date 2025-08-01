package net.mt1006.mocap.api.impl.controller.playable;

import net.mt1006.mocap.api.impl.controller.MocapControllerImpl;
import net.mt1006.mocap.api.impl.modifiers.MocapModifiersImpl;
import net.mt1006.mocap.api.v1.controller.playable.MocapPlayable;
import net.mt1006.mocap.api.v1.controller.playable.MocapScene;
import net.mt1006.mocap.api.v1.controller.playable.MocapSceneElement;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.mocap.files.Files;
import net.mt1006.mocap.mocap.files.SceneData;
import net.mt1006.mocap.mocap.files.SceneFiles;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MocapSceneImpl extends MocapPlayableImpl implements MocapScene
{
	private String name;

	public MocapSceneImpl(MocapControllerImpl ctrl, String name)
	{
		super(ctrl);
		this.name = name;
		if (!getId().startsWith(".")) { throw new RuntimeException("Failed to create MocapSceneImpl!"); }
	}

	@Override public String getId()
	{
		return name;
	}

	@Override public boolean exists()
	{
		return getFile() != null;
	}

	@Override public boolean remove()
	{
		return SceneFiles.remove(ctrl.commandInfo, name);
	}

	@Override public boolean rename(String name)
	{
		boolean success = SceneFiles.rename(ctrl.commandInfo, this.name, name);
		if (success) { this.name = name; }
		return success;
	}

	@Override public @Nullable MocapScene copy(String name)
	{
		boolean success = SceneFiles.copy(ctrl.commandInfo, this.name, name);
		return success ? new MocapSceneImpl(ctrl, name) : null;
	}

	@Override public @Nullable File getFile()
	{
		return Files.getRecordingFile(ctrl.commandInfo, name);
	}

	//TODO: finish
	@Override public boolean add(MocapSceneElement element)
	{
		return add(element.getPlayable(), element.getModifiers());
	}

	@Override public boolean add(MocapPlayable playable, @Nullable String playerName)
	{
		return add(playable, MocapModifiersImpl.withPlayerName(playerName));
	}

	@Override public boolean add(MocapPlayable playable, MocapModifiers modifiers)
	{
		return SceneFiles.addElement(ctrl.commandInfo, name, new SceneData.Subscene(playable.getId(), modifiers.getPlaybackModifiers()));
	}

	@Override public @Nullable List<? extends MocapSceneElement> getAll()
	{
		SceneData data = SceneFiles.loadSceneData(ctrl.commandInfo, name);
		if (data == null) { return null; }

		List<MocapSceneElement> elements = new ArrayList<>();
		data.subscenes.forEach((s) -> elements.add(new MocapSceneElementImpl(ctrl, s)));
		return Collections.unmodifiableList(elements);
	}

	@Override public boolean replaceAll(List<? extends MocapSceneElement> elements)
	{
		File file = Files.getSceneFile(ctrl.commandInfo, name);
		SceneData data = SceneFiles.loadSceneData(ctrl.commandInfo, file);
		if (data == null) { return false; }

		data.subscenes.clear();
		elements.forEach((e) -> data.subscenes.add(e.toSubscene()));
		return data.save(ctrl.commandInfo, file, name, "scenes.modify.success", "scenes.modify.error");
	}

	@Override public boolean clear()
	{
		File file = Files.getSceneFile(ctrl.commandInfo, name);
		SceneData data = SceneFiles.loadSceneData(ctrl.commandInfo, file);
		if (data == null) { return false; }

		data.subscenes.clear();
		return data.save(ctrl.commandInfo, file, name, "scenes.modify.success", "scenes.modify.error");
	}
}
