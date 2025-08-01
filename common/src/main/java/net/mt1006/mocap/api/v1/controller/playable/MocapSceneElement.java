package net.mt1006.mocap.api.v1.controller.playable;

import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.mocap.files.SceneData;
import org.jetbrains.annotations.ApiStatus;

public interface MocapSceneElement
{
	MocapPlayable getPlayable();

	MocapModifiers getModifiers();

	@ApiStatus.Internal
	SceneData.Subscene toSubscene();
}
