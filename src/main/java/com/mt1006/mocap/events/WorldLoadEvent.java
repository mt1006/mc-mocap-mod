package com.mt1006.mocap.events;

import com.mt1006.mocap.mocap.commands.Settings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public class WorldLoadEvent
{
	public static void onWorldUnload(MinecraftServer server, ServerLevel world)
	{
		Settings.unload();
	}
}
