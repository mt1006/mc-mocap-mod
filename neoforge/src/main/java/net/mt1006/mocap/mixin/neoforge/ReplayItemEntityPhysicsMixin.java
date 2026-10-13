package net.mt1006.mocap.mixin.neoforge;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.mt1006.mocap.mocap.playing.PlaybackManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class ReplayItemEntityPhysicsMixin
{
	@Inject(method = "moveTowardsClosestSpace(DDD)V", at = @At("HEAD"), cancellable = true)
	private void mocap$keepReplayItemsAtRecordedPositions(double x, double y, double z, CallbackInfo ci)
	{
		Entity entity = (Entity)(Object)this;
		if (entity instanceof ItemEntity && entity.entityTags().contains(PlaybackManager.MOCAP_ENTITY_TAG))
		{
			entity.setDeltaMovement(0.0, 0.0, 0.0);
			ci.cancel();
		}
	}
}