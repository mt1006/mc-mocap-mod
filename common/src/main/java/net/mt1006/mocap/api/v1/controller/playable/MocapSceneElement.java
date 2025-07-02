package net.mt1006.mocap.api.v1.controller.playable;

import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;

public interface MocapSceneElement
{
	MocapPlayable getPlayable();

	MocapModifiers getModifiers();
}
