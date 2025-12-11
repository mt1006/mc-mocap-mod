package com.mt1006.mocap.events;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.command.CommandOutput;
import com.mt1006.mocap.command.InputArgument;
import com.mt1006.mocap.mocap.files.Files;
import com.mt1006.mocap.mocap.playing.CustomClientSkinManager;
import com.mt1006.mocap.mocap.playing.Playing;
import com.mt1006.mocap.mocap.settings.Settings;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber(modid = MocapMod.MOD_ID)
public class WorldLoadEvent
{
	@SubscribeEvent
	public static void onServerStart(ServerStartedEvent startedEvent)
	{
		MinecraftServer server = startedEvent.getServer();
		InputArgument.initServerInputSet(server);
	}

	@SubscribeEvent
	public static void onServerStop(ServerStoppingEvent stoppingEvent)
	{
		Playing.stopAll(CommandOutput.DUMMY);
		Settings.unload();
		Files.deinitDirectories();
	}

	@SubscribeEvent
	public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut loggingOut)
	{
		InputArgument.clientInputSet.clear();
		PlayerConnectionEvent.players.clear();
		PlayerConnectionEvent.nocolPlayers.clear();
		CustomClientSkinManager.clearCache();
	}
}
