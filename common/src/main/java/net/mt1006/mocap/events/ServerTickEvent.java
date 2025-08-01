package net.mt1006.mocap.events;

import net.mt1006.mocap.mocap.playing.PlaybackManager;
import net.mt1006.mocap.mocap.recording.Recording;

public class ServerTickEvent
{
	public static void onEndTick()
	{
		Recording.onTick();
		PlaybackManager.onTick();
	}
}
