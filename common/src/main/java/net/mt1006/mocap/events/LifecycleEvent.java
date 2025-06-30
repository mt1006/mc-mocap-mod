package net.mt1006.mocap.events;

import net.minecraft.server.MinecraftServer;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.command.CommandSuggestions;
import net.mt1006.mocap.command.io.CommandOutput;
import net.mt1006.mocap.mocap.files.Files;
import net.mt1006.mocap.mocap.playing.Playing;
import net.mt1006.mocap.mocap.playing.skins.CustomClientSkinManager;
import net.mt1006.mocap.mocap.recording.Recording;
import net.mt1006.mocap.mocap.settings.Settings;

public class LifecycleEvent
{
	public static void onServerStart(MinecraftServer server)
	{
		MocapMod.server = server;
		Files.init();
		Settings.load();
		CommandSuggestions.refresh();
	}

	public static void onServerStop()
	{
		Playing.stopAll(CommandOutput.DUMMY, null);
		Settings.unload();
		Files.deinit();
		Recording.onServerStop();
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
