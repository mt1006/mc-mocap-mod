package com.mt1006.mocap.events;

import com.mt1006.mocap.command.CommandOutput;
import com.mt1006.mocap.command.InputArgument;
import com.mt1006.mocap.mocap.files.Files;
import com.mt1006.mocap.mocap.playing.CustomClientSkinManager;
import com.mt1006.mocap.mocap.playing.Playing;
import com.mt1006.mocap.mocap.settings.Settings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public class WorldLoadEvent
{
	public static void onServerWorldLoad(MinecraftServer server)
	{
		InputArgument.initServerInputSet(server);
	}

	public static void onServerWorldUnload(MinecraftServer server)
	{
		Playing.stopAll(CommandOutput.DUMMY);
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
