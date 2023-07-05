package com.mt1006.mocap.mixin;

import com.mojang.authlib.properties.Property;
import com.mt1006.mocap.mixin.fields.PlayerInfoMixin;
import com.mt1006.mocap.mocap.playing.CustomClientSkinManager;
import com.mt1006.mocap.mocap.playing.CustomSkinManager;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(AbstractClientPlayer.class)
abstract public class AbstractClientPlayerMixin
{
	@Shadow protected abstract @Nullable PlayerInfo getPlayerInfo();

	@Inject(method = "getSkinTextureLocation", at = @At(value = "HEAD"), cancellable = true)
	private void atGetSkinTextureLocation(CallbackInfoReturnable<ResourceLocation> cir)
	{
		PlayerInfo playerInfo = getPlayerInfo();
		if (playerInfo == null) { return; }

		Collection<Property> properties = playerInfo.getProfile().getProperties().get(CustomSkinManager.PROPERTY_ID);
		if (properties.size() < 1) { return; }

		ResourceLocation res = CustomClientSkinManager.get(properties.iterator().next().getValue());
		if (res == null) { return; }

		((PlayerInfoMixin)playerInfo).setSkinModel(CustomClientSkinManager.isSlimSkin(res) ? "slim" : "default");
		cir.setReturnValue(res);
		cir.cancel();
	}
}
