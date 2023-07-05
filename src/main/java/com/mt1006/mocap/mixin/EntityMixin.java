package com.mt1006.mocap.mixin;

import com.mt1006.mocap.mocap.playing.Playing;
import com.mt1006.mocap.mocap.settings.Settings;
import net.minecraft.world.entity.Entity;
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

	@Inject(method = "shouldBeSaved", at = @At(value = "HEAD"), cancellable = true)
	private void atShouldBeSaved(CallbackInfoReturnable<Boolean> cir)
	{
		if (Playing.playedScenes.size() > 0 && Settings.PREVENT_SAVING_ENTITIES.val && getTags().contains(Playing.MOCAP_ENTITY_TAG))
		{
			//TODO: check
			cir.setReturnValue(false);
			cir.cancel();
		}
	}
}
