package com.mt1006.mocap.mixin.fabric;

import com.mt1006.mocap.events.BlockInteractionEvent;
import com.mt1006.mocap.mocap.recording.Recording;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedItem.class)
public class BedItemMixin
{
	@Inject(method = "placeBlock", at = @At(value = "HEAD"))
	private void atPlaceBlock(BlockPlaceContext blockPlaceContext, BlockState blockState, CallbackInfoReturnable<Boolean> cir)
	{
		if (Recording.state == Recording.State.RECORDING && !blockPlaceContext.getLevel().isClientSide)
		{
			BlockPos pos = blockPlaceContext.getClickedPos();

			BlockInteractionEvent.onBlockPlace(blockPlaceContext.getPlayer(),
					blockPlaceContext.getLevel().getBlockState(pos), blockState, pos);
		}
	}

	@Inject(method = "placeBlock", at = @At(value = "TAIL"))
	private void atPlaceBlockEnd(BlockPlaceContext blockPlaceContext, BlockState blockState, CallbackInfoReturnable<Boolean> cir)
	{
		if (Recording.state == Recording.State.RECORDING && !blockPlaceContext.getLevel().isClientSide)
		{
			if (!( blockState.getBlock() instanceof BedBlock) || blockState.getValue(BedBlock.PART) != BedPart.FOOT) { return; }

			Direction direction = blockState.getValue(BedBlock.FACING);
			BlockState newBlockState = blockState.setValue(BedBlock.FACING, direction).setValue(BedBlock.PART, BedPart.HEAD);

			BlockPos pos = blockPlaceContext.getClickedPos();
			BlockPos secondPos = null;
			switch (direction)
			{
				case WEST: secondPos = pos.offset(-1, 0, 0); break;
				case EAST: secondPos = pos.offset(1, 0, 0); break;
				case NORTH: secondPos = pos.offset(0, 0, -1); break;
				case SOUTH: secondPos = pos.offset(0, 0, 1); break;
			}

			if (secondPos != null)
			{
				BlockInteractionEvent.onSilentBlockPlace(blockPlaceContext.getPlayer(),
						blockPlaceContext.getLevel().getBlockState(secondPos), newBlockState, secondPos);
			}
		}
	}
}
