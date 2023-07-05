package com.mt1006.mocap.mixin.fields;

import net.minecraft.client.network.play.NetworkPlayerInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NetworkPlayerInfo.class)
public interface PlayerInfoMixin
{
	@Accessor void setSkinModel(@Nullable String skinModel);
}
