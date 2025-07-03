package net.mt1006.mocap.neoforge.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.events.PlayerConnectionEvent;
import net.mt1006.mocap.neoforge.PacketHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = MocapMod.MOD_ID)
public class PlayerConnectionNeoForgeEvent
{
	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent loggedInEvent)
	{
		Player player = loggedInEvent.getEntity();
		if (!(player instanceof ServerPlayer)) { return; }

		PlayerConnectionEvent.onPlayerJoin(new PacketHandler.Client((ServerPlayer)player));
	}

	@SubscribeEvent
	public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent loggedOutEvent)
	{
		Player player = loggedOutEvent.getEntity();
		if (!(player instanceof ServerPlayer)) { return; }

		PlayerConnectionEvent.onPlayerLeave((ServerPlayer)player);
	}
}
