package com.mt1006.mocap.mixin.fabric;

import com.mt1006.mocap.events.BlockInteractionEvent;
import com.mt1006.mocap.mocap.recording.Recording;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DoubleHighBlockItem.class)
public class DoubleHighBlockItemMixin
{
	@Inject(method = "placeBlock", at = @At(value = "HEAD"))
	private void atPlaceBlock(BlockPlaceContext blockPlaceContext, BlockState blockState, CallbackInfoReturnable<Boolean> cir)
	{
		if (Recording.state == Recording.State.RECORDING && !blockPlaceContext.getLevel().isClientSide)
		{
			if (!blockState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
					|| blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.LOWER)
			{
				return;
			}

			BlockPos pos = blockPlaceContext.getClickedPos().offset(0, 1, 0);
			BlockState newBlockState = blockState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);

			BlockInteractionEvent.onSilentBlockPlace(blockPlaceContext.getPlayer(),
					blockPlaceContext.getLevel().getBlockState(pos), newBlockState, pos);
		}
	}
}
