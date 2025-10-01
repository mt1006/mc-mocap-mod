package com.mt1006.mocap.mixin;

import com.mojang.authlib.properties.Property;
import com.mt1006.mocap.mocap.playing.CustomClientSkinManager;
import com.mt1006.mocap.mocap.playing.CustomSkinManager;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
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

	@Inject(method = "getSkin", at = @At(value = "HEAD"), cancellable = true)
	private void atGetSkinTextureLocation(CallbackInfoReturnable<PlayerSkin> cir)
	{
		PlayerInfo playerInfo = getPlayerInfo();
		if (playerInfo == null) { return; }

		Collection<Property> properties = playerInfo.getProfile().properties().get(CustomSkinManager.PROPERTY_ID);
		if (properties.isEmpty()) { return; }

		ClientAsset.Texture skinTexture = CustomClientSkinManager.get(properties.iterator().next().value());
		if (skinTexture == null) { return; }

		PlayerSkin playerSkin = playerInfo.getSkin();

		cir.setReturnValue(new PlayerSkin(skinTexture, playerSkin.cape(), playerSkin.elytra(),
				CustomClientSkinManager.isSlimSkin(skinTexture) ? PlayerModelType.SLIM : PlayerModelType.WIDE,
				playerSkin.secure()));
		cir.cancel();
	}
}
