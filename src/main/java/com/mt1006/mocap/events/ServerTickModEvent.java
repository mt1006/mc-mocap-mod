package com.mt1006.mocap.events;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.mocap.playing.Playing;
import com.mt1006.mocap.mocap.recording.Recording;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = MocapMod.MOD_ID)
public class ServerTickModEvent
{
	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post tickEvent)
	{
		Recording.onTick();
		Playing.onTick();
	}
}
