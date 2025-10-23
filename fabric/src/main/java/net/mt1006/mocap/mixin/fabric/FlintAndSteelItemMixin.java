package net.mt1006.mocap.mixin.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.mt1006.mocap.events.BlockInteractionEvent;
import net.mt1006.mocap.mocap.recording.RecordingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.item.FlintAndSteelItem.class)
public class FlintAndSteelItemMixin
{
	@Inject(method = "useOn", at = @At(value = "HEAD"))
	private void atPlaceBlock(UseOnContext useOnContext, CallbackInfoReturnable<InteractionResult> cir)
	{
		if (RecordingManager.isActive() && !useOnContext.getLevel().isClientSide())
		{
			Player player = useOnContext.getPlayer();
			if (!RecordingManager.isRecordedPlayer(player)) { return; }

			Level level = useOnContext.getLevel();
			BlockPos blockPos = useOnContext.getClickedPos();
			BlockState blockState = level.getBlockState(blockPos);

			if (!CampfireBlock.canLight(blockState) && !CandleBlock.canLight(blockState) && !CandleCakeBlock.canLight(blockState))
			{
				BlockPos blockPos2 = blockPos.relative(useOnContext.getClickedFace());
				if (BaseFireBlock.canBePlacedAt(level, blockPos2, useOnContext.getHorizontalDirection()))
				{
					BlockState blockState2 = BaseFireBlock.getState(level, blockPos2);
					BlockInteractionEvent.onBlockPlace(player, useOnContext.getLevel().getBlockState(blockPos2), blockState2, blockPos2);
				}
			}
		}
	}
}
