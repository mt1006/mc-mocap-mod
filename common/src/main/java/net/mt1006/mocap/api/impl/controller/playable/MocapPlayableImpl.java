package net.mt1006.mocap.api.impl.controller.playable;

import net.mt1006.mocap.api.impl.controller.MocapControllerImpl;
import net.mt1006.mocap.api.v1.controller.MocapPlayback;
import net.mt1006.mocap.api.v1.controller.playable.MocapPlayable;
import net.mt1006.mocap.mocap.playing.Playing;
import net.mt1006.mocap.mocap.playing.modifiers.PlaybackModifiers;
import org.jetbrains.annotations.Nullable;

public abstract class MocapPlayableImpl implements MocapPlayable
{
	protected final MocapControllerImpl ctrl;

	public MocapPlayableImpl(MocapControllerImpl ctrl)
	{
		this.ctrl = ctrl;
	}

	@Override public @Nullable MocapPlayback startPlayback(String playerName)
	{
		//TODO: set playerName
		return Playing.startSingleSilently(ctrl.commandInfo, getId(), PlaybackModifiers.empty(), ctrl.hideStuff);
	}
}
