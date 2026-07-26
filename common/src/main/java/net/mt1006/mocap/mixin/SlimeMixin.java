package net.mt1006.mocap.mixin;

import net.minecraft.world.entity.monster.Slime;
import net.mt1006.mocap.mocap.playing.PlaybackManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Slime.class)
public class SlimeMixin
{
	@Redirect(method = "remove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Slime;isDeadOrDying()Z"))
	private boolean atRemove(Slime entity)
	{
		// fix slime splitting
		// (this might be called after playback is finished,
		// so we shouldn't optimize it by checking if any is active)

		boolean val = entity.isDeadOrDying();
		return val && !entity.entityTags().contains(PlaybackManager.MOCAP_ENTITY_TAG);
	}
}
