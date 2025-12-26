package net.mt1006.mocap.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.mt1006.mocap.mocap.playing.PlaybackManager;
import net.mt1006.mocap.mocap.settings.Settings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityMixin
{
	@Shadow public abstract Set<String> getTags();

	@Inject(method = "save", at = @At(value = "HEAD"), cancellable = true)
	private void atSave(CompoundTag compoundTag, CallbackInfoReturnable<Boolean> cir)
	{
		if (!PlaybackManager.playbacks.isEmpty() && Settings.PREVENT_SAVING_ENTITIES.val && getTags().contains(PlaybackManager.MOCAP_ENTITY_TAG))
		{
			cir.setReturnValue(false);
			cir.cancel();
		}
	}
}
