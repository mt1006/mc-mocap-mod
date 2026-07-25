package net.mt1006.mocap.neoforge.events;

import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.events.LifecycleEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber(modid = MocapMod.MOD_ID)
public class LifecycleNeoForgeEvent
{
	@SubscribeEvent
	public static void onServerStart(ServerStartedEvent event)
	{
		LifecycleEvent.onServerStart(event.getServer());
	}

	@SubscribeEvent
	public static void onServerStop(ServerStoppingEvent event)
	{
		LifecycleEvent.onServerStop(event.getServer());
	}

	@SubscribeEvent
	public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event)
	{
		LifecycleEvent.onClientDisconnect();
	}
}
