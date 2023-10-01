package com.mt1006.mocap.mixin.fabric;

import com.mt1006.mocap.events.WorldLoadEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin
{
	// Based on Forge LevelEvent.Unload injections

	@Shadow @Nullable public ClientLevel level;

	@Inject(method = "setLevel", at = @At(value = "HEAD"))
	public void atSetLevel(ClientLevel clientLevel, CallbackInfo callbackInfo)
	{
		if (level != null) { WorldLoadEvent.onClientWorldUnload(); }
	}

	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At(value = "HEAD"))
	public void atDisconnect(Screen screen, CallbackInfo ci)
	{
		if (level != null) { WorldLoadEvent.onClientWorldUnload(); }
	}
}
