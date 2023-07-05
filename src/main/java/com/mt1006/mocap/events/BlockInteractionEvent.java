package com.mt1006.mocap.events;

import com.mt1006.mocap.mocap.actions.BreakBlock;
import com.mt1006.mocap.mocap.actions.PlaceBlock;
import com.mt1006.mocap.mocap.actions.PlaceBlockSilently;
import com.mt1006.mocap.mocap.actions.RightClickBlock;
import com.mt1006.mocap.mocap.recording.Recording;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockInteractionEvent
{
	public static boolean onBlockBreak(Level level, Player player, BlockPos pos, BlockState blockState, @Nullable BlockEntity blockEntity)
	{
		if (Recording.state == Recording.State.RECORDING && Recording.isRecordedPlayer(player))
		{
			new BreakBlock(blockState, pos).write(Recording.writer);
		}
		return true;
	}

	public static void onBlockPlace(Player player, BlockState replacedBlock, BlockState placedBlock, BlockPos blockPos)
	{
		if (Recording.isRecordedPlayer(player))
		{
			new PlaceBlock(replacedBlock, placedBlock, blockPos).write(Recording.writer);
		}
	}

	public static void onSilentBlockPlace(Player player, BlockState replacedBlock, BlockState placedBlock, BlockPos blockPos)
	{
		if (Recording.isRecordedPlayer(player))
		{
			new PlaceBlockSilently(replacedBlock, placedBlock, blockPos).write(Recording.writer);
		}
	}

	public static InteractionResult onRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult)
	{
		if (Recording.state == Recording.State.RECORDING && Recording.isRecordedPlayer(player) && !usedOnShift(player, hitResult.getBlockPos()))
		{
			new RightClickBlock(hitResult, hand == InteractionHand.OFF_HAND).write(Recording.writer);
		}
		return InteractionResult.PASS;
	}

	private static boolean usedOnShift(Player player, BlockPos blockPos)
	{
		return player.isSecondaryUseActive() && (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty());
	}
}
