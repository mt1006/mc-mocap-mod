package com.mt1006.mocap.mixin;

import com.mojang.authlib.GameProfile;
import com.mt1006.mocap.mocap.playing.CustomSkinManager;
import net.minecraft.client.resources.SkinManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkinManager.class)
public class SkinManagerMixin
{
	@Inject(method = "registerSkins", at = @At(value = "HEAD"), cancellable = true)
	public void atRegisterSkins(GameProfile profile, SkinManager.ISkinAvailableCallback callback, boolean b, CallbackInfo callbackInfo)
	{
		if (profile.getProperties().containsKey(CustomSkinManager.PROPERTY_ID)) { callbackInfo.cancel(); }
	}
}
