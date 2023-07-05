package com.mt1006.mocap.mixin;

import com.mt1006.mocap.events.EntityEvent;
import com.mt1006.mocap.events.PlayerConnectionEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMethodMixin
{
	@Inject(method = "doPush", at = @At("HEAD"), cancellable = true)
	private void atDoPush(Entity entity, CallbackInfo callbackInfo)
	{
		if (PlayerConnectionEvent.nocolPlayers.size() > 0 && (Object)this != entity)
		{
			if (PlayerConnectionEvent.nocolPlayers.contains(entity.getUUID())
					|| PlayerConnectionEvent.nocolPlayers.contains(((Entity)(Object)this).getUUID()))
			{
				callbackInfo.cancel();
			}
		}
	}

	// Fabric-only - based on Forge LivingDropsEvent injection

	@Inject(method = "dropAllDeathLoot", at = @At("HEAD"), cancellable = true)
	private void atDropAllDeathLoot(DamageSource damageSource, CallbackInfo callbackInfo)
	{
		if (EntityEvent.onEntityDrop((LivingEntity)(Object)this)) { callbackInfo.cancel(); }
	}
}
