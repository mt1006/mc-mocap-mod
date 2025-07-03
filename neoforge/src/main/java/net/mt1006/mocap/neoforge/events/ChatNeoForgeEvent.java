package net.mt1006.mocap.neoforge.events;

import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.events.ChatEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

@EventBusSubscriber(modid = MocapMod.MOD_ID)
public class ChatNeoForgeEvent
{
	@SubscribeEvent
	public static void onChatMessage(ServerChatEvent chatEvent)
	{
		ChatEvent.onChatMessage(chatEvent.getMessage(), chatEvent.getPlayer());
	}
}
