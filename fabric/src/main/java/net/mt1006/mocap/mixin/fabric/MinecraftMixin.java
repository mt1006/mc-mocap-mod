package net.mt1006.mocap.mixin.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.mt1006.mocap.events.LifecycleEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin
{
	@Shadow @Nullable public ClientLevel level;

	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At(value = "HEAD"))
	public void atDisconnect(Screen nextScreen, boolean keepResourcePacks, CallbackInfo ci)
	{
		if (level != null) { LifecycleEvent.onClientDisconnect(); }
	}
}
