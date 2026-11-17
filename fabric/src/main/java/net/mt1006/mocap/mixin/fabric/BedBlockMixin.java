package net.mt1006.mocap.mixin.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.mt1006.mocap.events.BlockInteractionEvent;
import net.mt1006.mocap.mocap.recording.RecordingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBedBlock.class)
public class BedBlockMixin
{
	@Inject(method = "setPlacedBy", at = @At(value = "TAIL"))
	private void atSetPlacedBy(Level level, BlockPos pos, BlockState blockState, LivingEntity placer, ItemStack stack,
							   CallbackInfo ci)
	{
		if (!RecordingManager.isActive() || level.isClientSide() || !(placer instanceof Player player)) { return; }

		BlockPos headPos = pos.relative(blockState.getValue(AbstractBedBlock.FACING));
		BlockState headState = blockState.setValue(AbstractBedBlock.PART, BedPart.HEAD);

		BlockInteractionEvent.onSilentBlockPlace(player, level.getBlockState(headPos), headState, headPos);
	}
}
