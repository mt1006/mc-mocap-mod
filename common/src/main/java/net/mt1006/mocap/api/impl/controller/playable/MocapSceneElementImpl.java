package net.mt1006.mocap.api.impl.controller.playable;

import net.mt1006.mocap.api.impl.controller.MocapControllerImpl;
import net.mt1006.mocap.api.impl.modifiers.MocapModifiersImpl;
import net.mt1006.mocap.api.v1.controller.playable.MocapPlayable;
import net.mt1006.mocap.api.v1.controller.playable.MocapSceneElement;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.mocap.files.SceneData;

public class MocapSceneElementImpl implements MocapSceneElement
{
	private final MocapPlayable playable;
	private final MocapModifiers modifiers;

	public MocapSceneElementImpl(MocapControllerImpl ctrl, SceneData.Subscene subscene)
	{
		//TODO: test length 0
		this.playable = MocapPlayableImpl.fromName(ctrl, subscene.name);
		this.modifiers = MocapModifiersImpl.ofCopy(subscene.modifiers);
	}

	@Override public MocapPlayable getPlayable()
	{
		return playable;
	}

	@Override public MocapModifiers getModifiers()
	{
		return modifiers;
	}

	@Override public SceneData.Subscene toSubscene()
	{
		return new SceneData.Subscene(playable.getId(), modifiers.getPlaybackModifiers());
	}
}
