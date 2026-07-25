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
	//TODO: remove all ...Event to event
	@SubscribeEvent
	public static void onServerStart(ServerStartedEvent startedEvent)
	{
		LifecycleEvent.onServerStart(startedEvent.getServer());
	}

	@SubscribeEvent
	public static void onServerStop(ServerStoppingEvent stoppingEvent)
	{
		LifecycleEvent.onServerStop(stoppingEvent.getServer());
	}

	@SubscribeEvent
	public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut loggingOutEvent)
	{
		LifecycleEvent.onClientDisconnect();
	}
}
