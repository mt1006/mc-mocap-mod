package net.mt1006.mocap.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin
{
	@Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getTeam()Lnet/minecraft/world/scores/PlayerTeam;"), cancellable = true)
	private void atShouldShowName(LivingEntity entity, double distanceSq, CallbackInfoReturnable<Boolean> cir)
	{
		if (entity instanceof AbstractClientPlayer && ((AbstractClientPlayer)entity).getGameProfile().name().isEmpty())
		{
			cir.setReturnValue(false);
			cir.cancel();
		}
	}
}
