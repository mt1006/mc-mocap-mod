package com.mt1006.mocap.mixin;

import com.mojang.authlib.properties.Property;
import com.mt1006.mocap.mixin.fields.PlayerInfoMixin;
import com.mt1006.mocap.mocap.playing.CustomClientSkinManager;
import com.mt1006.mocap.mocap.playing.CustomSkinManager;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(AbstractClientPlayerEntity.class)
abstract public class AbstractClientPlayerMixin
{
	@Shadow protected abstract @Nullable NetworkPlayerInfo getPlayerInfo();

	@Inject(method = "getSkinTextureLocation", at = @At(value = "HEAD"), cancellable = true)
	private void atGetSkinTextureLocation(CallbackInfoReturnable<ResourceLocation> cir)
	{
		NetworkPlayerInfo playerInfo = getPlayerInfo();
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
