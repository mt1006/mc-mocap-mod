package com.mt1006.mocap.mixin.fields;

import net.minecraft.client.multiplayer.PlayerInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerInfo.class)
public interface PlayerInfoMixin
{
	@Accessor void setSkinModel(@Nullable String skinModel);
}
