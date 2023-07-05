package com.mt1006.mocap.events;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.command.InputArgument;
import com.mt1006.mocap.mocap.files.Files;
import com.mt1006.mocap.mocap.playing.CustomClientSkinManager;
import com.mt1006.mocap.mocap.playing.Playing;
import com.mt1006.mocap.mocap.settings.Settings;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MocapMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldLoadEvent
{
	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load loadEvent)
	{
		if (!loadEvent.getWorld().isClientSide())
		{
			MinecraftServer server = loadEvent.getWorld().getServer();
			if (server != null) { InputArgument.initServerInputSet(server); }
		}
	}

	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload unloadEvent)
	{
		if (unloadEvent.getWorld().isClientSide())
		{
			InputArgument.clientInputSet.clear();
			PlayerConnectionEvent.players.clear();
			PlayerConnectionEvent.nocolPlayers.clear();
			CustomClientSkinManager.clearCache();
		}
		else
		{
			Playing.stopAll(null);
			Settings.unload();
			Files.deinitDirectories();
		}
	}
}
