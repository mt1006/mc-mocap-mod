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
		this.playable = switch (subscene.name.charAt(0))
		{
			case '.' -> new MocapSceneImpl(ctrl, subscene.name);
			case '-' -> new MocapActiveRecordingImpl(ctrl, subscene.name);
			default -> new MocapSavedRecordingImpl(ctrl, subscene.name);
		};
		this.modifiers = MocapModifiersImpl.ofCopy(subscene.modifiers);
	}

	public SceneData.Subscene toSubscene()
	{
		if (!(modifiers instanceof MocapModifiersImpl)) { throw new RuntimeException("MocapModifiers isn't instance of MocapModifiersImpl!"); }
		return new SceneData.Subscene(playable.getId(), ((MocapModifiersImpl)modifiers).getPlaybackModifiers());
	}

	@Override public MocapPlayable getPlayable()
	{
		return playable;
	}

	@Override public MocapModifiers getModifiers()
	{
		return modifiers;
	}
}
