package com.mt1006.mocap.mixin;

import com.mt1006.mocap.events.BlockInteractionEvent;
import com.mt1006.mocap.mocap.commands.Recording;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin
{
	@Inject(method = "placeBlock", at = @At(value = "HEAD"))
	private void useOnMixin(BlockPlaceContext blockPlaceContext, BlockState blockState, CallbackInfoReturnable<Boolean> callbackInfo)
	{
		if (Recording.state == Recording.State.RECORDING && !blockPlaceContext.getLevel().isClientSide)
		{
			BlockPos pos = blockPlaceContext.getClickedPos();

			BlockInteractionEvent.onBlockPlace(blockPlaceContext.getPlayer(),
					blockPlaceContext.getLevel().getBlockState(pos), blockState, pos);
		}
	}
}
