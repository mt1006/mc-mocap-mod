package net.mt1006.mocap.neoforge.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.events.BlockInteractionEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = MocapMod.MOD_ID)
public class BlockInteractionNeoForgeEvent
{
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event)
	{
		BlockInteractionEvent.onBlockBreak(event.getPlayer(), event.getPos(), event.getState());
	}

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.EntityPlaceEvent event)
	{
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) { return; }

		BlockInteractionEvent.onBlockPlace((Player)entity,
				event.getBlockSnapshot().getState(),
				event.getPlacedBlock(), event.getPos());
	}

	@SubscribeEvent
	public static void onBlockPlaceSilently(BlockEvent.EntityMultiPlaceEvent event)
	{
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) { return; }

		BlockInteractionEvent.onSilentBlockPlace((Player)entity,
				event.getBlockSnapshot().getState(),
				event.getPlacedBlock(), event.getPos());
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
	{
		Player player = event.getEntity();

		BlockInteractionEvent.onRightClickBlock(player, event.getHand(), event.getHitVec(),
				player.getMainHandItem().doesSneakBypassUse(player.level(), event.getPos(), player));
	}

	@SubscribeEvent
	public static void onContainerClose(PlayerContainerEvent.Close event)
	{
		BlockInteractionEvent.onContainerClose(event.getEntity(), event.getContainer());
	}
}
