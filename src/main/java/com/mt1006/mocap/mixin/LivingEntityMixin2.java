package com.mt1006.mocap.mixin;

import com.mt1006.mocap.events.PlayerConnectionEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin2
{
	@Inject(method = "pushEntities", at = @At("HEAD"), cancellable = true)
	private void atPushEntities(CallbackInfo callbackInfo)
	{
		if (((Object)this) instanceof Player &&
				PlayerConnectionEvent.nocolPlayers.size() > 0 &&
				PlayerConnectionEvent.nocolPlayers.contains(((Player)(Object)this).getUUID()))
		{
			callbackInfo.cancel();
		}
	}
}
