package net.mt1006.mocap.api.v1.controller.playable;

import net.mt1006.mocap.api.v1.controller.MocapFile;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.mocap.files.SceneData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MocapSceneFile extends MocapPlayable, MocapFile<MocapSceneFile>
{
	default boolean add(CommandOutput out, MocapPlayable playable)
	{
		return add(out, new SceneData.Element(playable.getName(), MocapModifiers.empty()));
	}

	default boolean add(CommandOutput out, MocapPlayable playable, MocapModifiers modifiers)
	{
		return add(out, new SceneData.Element(playable.getName(), modifiers));
	}

	default boolean add(CommandOutput out, String playableName, MocapModifiers modifiers)
	{
		return add(out, new SceneData.Element(playableName, modifiers));
	}

	boolean add(CommandOutput out, MocapSceneElement element);

	@Nullable List<? extends MocapSceneElement> getAll(CommandOutput out);

	boolean replaceAll(CommandOutput out, List<? extends MocapSceneElement> elements);

	boolean clear(CommandOutput out);
}
