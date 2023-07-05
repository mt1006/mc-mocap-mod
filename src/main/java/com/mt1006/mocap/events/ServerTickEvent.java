package com.mt1006.mocap.events;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.mocap.playing.Playing;
import com.mt1006.mocap.mocap.recording.Recording;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MocapMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerTickEvent
{
	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent tickEvent)
	{
		if (tickEvent.phase == TickEvent.Phase.END)
		{
			Recording.onTick();
			Playing.onTick();
		}
	}
}
