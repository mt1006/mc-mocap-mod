package com.mt1006.mocap.events;

import com.mt1006.mocap.mocap.actions.BreakBlock;
import com.mt1006.mocap.mocap.actions.PlaceBlock;
import com.mt1006.mocap.mocap.actions.RightClickBlock;
import com.mt1006.mocap.mocap.commands.Recording;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
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
		if (Recording.state == Recording.State.RECORDING && isRecordedPlayer(player))
		{
			new BreakBlock(blockState, pos).write(Recording.recording);
		}
		return true;
	}

	public static void onBlockPlace(Player player, BlockState replacedBlock, BlockState placedBlock, BlockPos blockPos)
	{
		if (Recording.state == Recording.State.RECORDING && isRecordedPlayer(player))
		{
			new PlaceBlock(replacedBlock, placedBlock, blockPos).write(Recording.recording);
		}
	}

	public static InteractionResult onRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult)
	{
		if (Recording.state == Recording.State.RECORDING && isRecordedPlayer(player) && !usedOnShift(player, hitResult.getBlockPos()))
		{
			new RightClickBlock(hitResult, hand == InteractionHand.OFF_HAND).write(Recording.recording);
		}
		return InteractionResult.PASS;
	}

	private static boolean isRecordedPlayer(@Nullable Entity entity)
	{
		return (entity instanceof ServerPlayer) && entity.equals(Recording.serverPlayer);
	}

	private static boolean usedOnShift(Player player, BlockPos blockPos)
	{
		return player.isSecondaryUseActive() && (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty());
	}
}
