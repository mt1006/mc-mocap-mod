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
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid = MocapMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class WorldLoadEvent
{
	@SubscribeEvent
	public static void onWorldLoad(LevelEvent.Load loadEvent)
	{
		if (!loadEvent.getLevel().isClientSide())
		{
			MinecraftServer server = loadEvent.getLevel().getServer();
			if (server != null) { InputArgument.initServerInputSet(server); }
		}
	}

	@SubscribeEvent
	public static void onWorldUnload(LevelEvent.Unload unloadEvent)
	{
		if (unloadEvent.getLevel().isClientSide())
		{
			InputArgument.clientInputSet.clear();
			PlayerConnectionEvent.players.clear();
			PlayerConnectionEvent.nocolPlayers.clear();
			CustomClientSkinManager.clearCache();
		}
		else
		{
			Playing.stopAll(CommandOutput.DUMMY);
			Settings.unload();
			Files.deinitDirectories();
		}
	}
}
