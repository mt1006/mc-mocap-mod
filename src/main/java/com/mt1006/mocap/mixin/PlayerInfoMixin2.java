package com.mt1006.mocap.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mt1006.mocap.mocap.playing.CustomSkinManager;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(PlayerInfo.class)
public abstract class PlayerInfoMixin2
{
	@Shadow private @Nullable String skinModel;

	@Shadow public abstract GameProfile getProfile();

	@Inject(method = "getModelName", at = @At(value = "HEAD"), cancellable = true)
	private void hdSkinsFix(CallbackInfoReturnable<String> cir)
	{
		Collection<Property> properties = getProfile().getProperties().get(CustomSkinManager.PROPERTY_ID);
		if (!properties.isEmpty() && skinModel != null)
		{
			cir.setReturnValue(skinModel);
			cir.cancel();
		}
	}
}
