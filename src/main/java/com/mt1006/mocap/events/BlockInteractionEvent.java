package com.mt1006.mocap.events;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.mocap.actions.BreakBlock;
import com.mt1006.mocap.mocap.actions.PlaceBlock;
import com.mt1006.mocap.mocap.actions.PlaceBlockSilently;
import com.mt1006.mocap.mocap.actions.RightClickBlock;
import com.mt1006.mocap.mocap.recording.Recording;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MocapMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlockInteractionEvent
{
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent breakEvent)
	{
		if (Recording.state == Recording.State.RECORDING && Recording.isRecordedPlayer(breakEvent.getPlayer()))
		{
			new BreakBlock(breakEvent.getState(), breakEvent.getPos()).write(Recording.writer);
		}
	}

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.EntityPlaceEvent placeEvent)
	{
		if (Recording.state == Recording.State.RECORDING && Recording.isRecordedPlayer(placeEvent.getEntity()))
		{
			new PlaceBlock(placeEvent.getBlockSnapshot().getReplacedBlock(),
					placeEvent.getPlacedBlock(), placeEvent.getPos()).write(Recording.writer);
		}
	}

	@SubscribeEvent
	public static void onBlockPlaceSilently(BlockEvent.EntityMultiPlaceEvent multiPlaceEvent)
	{
		if (Recording.state == Recording.State.RECORDING && Recording.isRecordedPlayer(multiPlaceEvent.getEntity()))
		{
			BlockSnapshot mainSnapshot = multiPlaceEvent.getBlockSnapshot();
			for (BlockSnapshot snapshot : multiPlaceEvent.getReplacedBlockSnapshots())
			{
				if (snapshot.getPos().equals(mainSnapshot.getPos())) { continue; }
				new PlaceBlockSilently(snapshot.getReplacedBlock(), snapshot.getCurrentBlock(), snapshot.getPos()).write(Recording.writer);
			}
		}
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock clickEvent)
	{
		if (Recording.state == Recording.State.RECORDING && Recording.isRecordedPlayer(clickEvent.getEntity()) &&
				!usedOnShift(clickEvent.getEntity(), clickEvent.getPos()))
		{
			new RightClickBlock(clickEvent.getHitVec(), clickEvent.getHand() == InteractionHand.OFF_HAND).write(Recording.writer);
		}
	}

	private static boolean usedOnShift(Player player, BlockPos blockPos)
	{
		return player.isSecondaryUseActive() && (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) &&
				!(player.getMainHandItem().doesSneakBypassUse(player.level, blockPos, player) &&
						player.getOffhandItem().doesSneakBypassUse(player.level, blockPos, player));
	}
}
