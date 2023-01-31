package com.mt1006.mocap.events;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.mocap.actions.BreakBlock;
import com.mt1006.mocap.mocap.actions.PlaceBlock;
import com.mt1006.mocap.mocap.actions.RightClickBlock;
import com.mt1006.mocap.mocap.commands.Recording;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = MocapMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlockInteractionEvent
{
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent breakEvent)
	{
		if (Recording.state == Recording.State.RECORDING && isRecordedPlayer(breakEvent.getPlayer()))
		{
			new BreakBlock(breakEvent.getState(), breakEvent.getPos()).write(Recording.recording);
		}
	}

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.EntityPlaceEvent placeEvent)
	{
		if (Recording.state == Recording.State.RECORDING && isRecordedPlayer(placeEvent.getEntity()))
		{
			new PlaceBlock(placeEvent.getBlockSnapshot().getReplacedBlock(),
					placeEvent.getPlacedBlock(), placeEvent.getPos()).write(Recording.recording);
		}
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock clickEvent)
	{
		if (Recording.state == Recording.State.RECORDING && isRecordedPlayer(clickEvent.getEntity()) &&
				!usedOnShift(clickEvent.getPlayer(), clickEvent.getPos()))
		{
			new RightClickBlock(clickEvent.getHitVec(), clickEvent.getHand() == Hand.OFF_HAND).write(Recording.recording);
		}
	}

	private static boolean isRecordedPlayer(@Nullable Entity entity)
	{
		return (entity instanceof ServerPlayerEntity) && entity.equals(Recording.serverPlayer);
	}

	private static boolean usedOnShift(PlayerEntity player, BlockPos blockPos)
	{
		return player.isSecondaryUseActive() && (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) &&
				!(player.getMainHandItem().doesSneakBypassUse(player.level, blockPos, player) &&
						player.getOffhandItem().doesSneakBypassUse(player.level, blockPos, player));
	}
}
