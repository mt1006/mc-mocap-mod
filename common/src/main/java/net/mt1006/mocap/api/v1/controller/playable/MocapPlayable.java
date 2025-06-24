package net.mt1006.mocap.api.v1.controller.playable;

import net.mt1006.mocap.api.v1.controller.MocapPlayback;
import org.jetbrains.annotations.Nullable;

public interface MocapPlayable
{
	String getId();

	boolean exists();

	@Nullable MocapPlayback startPlayback(String playerName);
}
