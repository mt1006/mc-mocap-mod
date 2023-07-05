package com.mt1006.mocap.events;

import com.mt1006.mocap.command.InputArgument;
import com.mt1006.mocap.mocap.files.Files;
import com.mt1006.mocap.mocap.playing.CustomClientSkinManager;
import com.mt1006.mocap.mocap.playing.CustomSkinManager;
import com.mt1006.mocap.mocap.playing.Playing;
import com.mt1006.mocap.mocap.settings.Settings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public class WorldLoadEvent
{
	public static void onServerWorldLoad(MinecraftServer server, ServerLevel world)
	{
		InputArgument.initServerInputSet(world.getServer());
	}

	public static void onServerWorldUnload(MinecraftServer server, ServerLevel world)
	{
		Playing.stopAll(null);
		Settings.unload();
		Files.deinitDirectories();
	}

	public static void onClientWorldUnload()
	{
		InputArgument.clientInputSet.clear();
		PlayerConnectionEvent.players.clear();
		PlayerConnectionEvent.nocolPlayers.clear();
		CustomClientSkinManager.clearCache();
	}
}
