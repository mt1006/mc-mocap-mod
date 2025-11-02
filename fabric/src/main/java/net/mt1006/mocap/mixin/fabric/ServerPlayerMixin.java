package net.mt1006.mocap.mixin.fabric;

import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.events.BlockInteractionEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin
{
	@Inject(method = "doCloseContainer", at = @At(value = "HEAD"))
	private void atDoCloseContainer(CallbackInfo ci)
	{
		ServerPlayer player = (ServerPlayer)(Object)this;
		BlockInteractionEvent.onContainerClose(player, player.containerMenu);
	}
}
