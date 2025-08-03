package net.mt1006.mocap.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.mt1006.mocap.mocap.actions.BreakBlock;
import net.mt1006.mocap.mocap.actions.PlaceBlock;
import net.mt1006.mocap.mocap.actions.PlaceBlockSilently;
import net.mt1006.mocap.mocap.actions.RightClickBlock;
import net.mt1006.mocap.mocap.recording.RecordingManager;

public class BlockInteractionEvent
{
	public static void onBlockBreak(Player player, BlockPos pos, BlockState blockState)
	{
		if (RecordingManager.isActive())
		{
			RecordingManager.byRecordedPlayer(player).forEach((ctx) -> ctx.addAction(new BreakBlock(blockState, pos)));
		}
	}

	public static void onBlockPlace(Player player, BlockState replacedBlock, BlockState placedBlock, BlockPos blockPos)
	{
		if (RecordingManager.isActive())
		{
			RecordingManager.byRecordedPlayer(player).forEach((ctx) -> ctx.addAction(new PlaceBlock(replacedBlock, placedBlock, blockPos)));
		}
	}

	public static void onSilentBlockPlace(Player player, BlockState replacedBlock, BlockState placedBlock, BlockPos blockPos)
	{
		if (RecordingManager.isActive())
		{
			RecordingManager.byRecordedPlayer(player).forEach((ctx) -> ctx.addAction(new PlaceBlockSilently(replacedBlock, placedBlock, blockPos)));
		}
	}

	public static void onRightClickBlock(Player player, InteractionHand hand, BlockHitResult hitResult, boolean doesSneakBypassUse)
	{
		if (RecordingManager.isActive() && !usedOnShift(player, doesSneakBypassUse))
		{
			boolean isOffHand = (hand == InteractionHand.OFF_HAND);
			RecordingManager.byRecordedPlayer(player).forEach((ctx) -> ctx.addAction(new RightClickBlock(hitResult, isOffHand)));
		}
	}

	private static boolean usedOnShift(Player player, boolean doesSneakBypassUse)
	{
		return player.isSecondaryUseActive()
				&& (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty())
				&& !doesSneakBypassUse;
	}
}
