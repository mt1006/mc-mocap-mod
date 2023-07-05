package com.mt1006.mocap.events;

import com.mt1006.mocap.mocap.playing.Playing;
import com.mt1006.mocap.mocap.recording.Recording;
import net.minecraft.server.MinecraftServer;

public class ServerTickEvent
{
	public static void onEndTick(MinecraftServer server)
	{
		Recording.onTick();
		Playing.onTick();
	}
}
