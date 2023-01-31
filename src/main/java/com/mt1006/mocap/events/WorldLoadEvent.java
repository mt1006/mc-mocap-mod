package com.mt1006.mocap.events;

import com.mt1006.mocap.mocap.commands.Playing;
import com.mt1006.mocap.mocap.commands.Settings;
import com.mt1006.mocap.mocap.files.Files;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public class WorldLoadEvent
{
	public static void onWorldUnload(MinecraftServer server, ServerLevel world)
	{
		Playing.stopAll(null);
		Settings.unload();
		Files.deinitDirectories();
	}
}