package net.mt1006.mocap.events;

import net.minecraft.server.MinecraftServer;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.v1.events.MocapEvents;
import net.mt1006.mocap.command.CommandSuggestions;
import net.mt1006.mocap.mocap.files.Files;
import net.mt1006.mocap.mocap.playing.PlaybackManager;
import net.mt1006.mocap.mocap.playing.skins.CustomClientSkinManager;
import net.mt1006.mocap.mocap.recording.RecordingManager;
import net.mt1006.mocap.mocap.settings.Settings;

public class LifecycleEvent
{
	public static void onServerStart(MinecraftServer server)
	{
		MocapMod.server = server;

		MocapEvents.SERVER_START_PRE.invoker.onServerStartPre(server);
		Files.init();
		Settings.load();
		CommandSuggestions.refresh();
		MocapEvents.SERVER_START_POST.invoker.onServerStartPost(server);
	}

	public static void onServerStop(MinecraftServer server)
	{
		MocapEvents.SERVER_STOP_PRE.invoker.onServerStopPre(server);
		PlaybackManager.onServerStop();
		Settings.unload();
		Files.deinit();
		RecordingManager.onServerStop();
		MocapEvents.SERVER_STOP_POST.invoker.onServerStopPost(server);

		MocapMod.server = null;
	}

	public static void onClientDisconnect()
	{
		// clearing integrated server cache on render (client) thread (it's never called on a dedicated server)
		PlayerConnectionEvent.players.clear();
		PlayerConnectionEvent.nocolPlayers.clear();

		CustomClientSkinManager.clearCache();
	}
}
