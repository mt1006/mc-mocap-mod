package net.mt1006.mocap.api.v1.controller.playable;

import net.mt1006.mocap.api.v1.controller.MocapFile;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MocapScene extends MocapPlayable, MocapFile<MocapScene>
{
	boolean add(MocapSceneElement element);

	boolean add(MocapPlayable playable, @Nullable String playerName);

	boolean add(MocapPlayable playable, MocapModifiers modifiers);

	@Nullable List<? extends MocapSceneElement> getAll();

	boolean replaceAll(List<? extends MocapSceneElement> elements);

	boolean clear();
}
