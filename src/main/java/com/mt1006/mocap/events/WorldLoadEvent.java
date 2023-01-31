package com.mt1006.mocap.events;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.mocap.commands.Playing;
import com.mt1006.mocap.mocap.commands.Settings;
import com.mt1006.mocap.mocap.files.Files;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MocapMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldLoadEvent
{
	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload unloadEvent)
	{
		if (!unloadEvent.getWorld().isClientSide())
		{
			Playing.stopAll(null);
			Settings.unload();
			Files.deinitDirectories();
		}
	}
}
