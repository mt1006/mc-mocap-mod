package net.mt1006.mocap.mixin;

import com.mojang.authlib.properties.Property;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.mt1006.mocap.mocap.playing.skins.CustomClientSkinManager;
import net.mt1006.mocap.mocap.playing.skins.CustomServerSkinManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin
{
	@Shadow protected abstract @Nullable PlayerInfo getPlayerInfo();

	@Inject(method = "getSkin", at = @At(value = "HEAD"), cancellable = true)
	private void atGetSkinTextureLocation(CallbackInfoReturnable<PlayerSkin> cir)
	{
		PlayerInfo playerInfo = getPlayerInfo();
		if (playerInfo == null) { return; }

		Collection<Property> properties = playerInfo.getProfile().getProperties().get(CustomServerSkinManager.PROPERTY_ID);
		if (properties.isEmpty()) { return; }

		ResourceLocation id = CustomClientSkinManager.get(properties.iterator().next().value());
		if (id == null) { return; }

		PlayerSkin playerSkin = playerInfo.getSkin();

		cir.setReturnValue(new PlayerSkin(id,
				playerSkin.textureUrl(), playerSkin.capeTexture(), playerSkin.elytraTexture(),
				CustomClientSkinManager.isSlimSkin(id) ? PlayerSkin.Model.SLIM : PlayerSkin.Model.WIDE,
				playerSkin.secure()));
		cir.cancel();
	}
}
