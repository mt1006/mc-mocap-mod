package net.mt1006.mocap.mocap.playing.playable;

import net.mt1006.mocap.api.v1.controller.playable.MocapPlayable;

public abstract class Playable implements MocapPlayable
{
	@Override public boolean equals(Object obj)
	{
		return (obj instanceof Playable && ((Playable)obj).getName().equals(getName()));
	}

	@Override public int hashCode()
	{
		return getName().hashCode();
	}
}
