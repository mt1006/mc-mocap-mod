package com.mt1006.mocap.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.datasync.DataParameter;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerEntity.class)
public interface PlayerMixin
{
	@Accessor static @Nullable DataParameter<Byte> getDATA_PLAYER_MODE_CUSTOMISATION() { return null; }
	@Accessor static @Nullable DataParameter<Byte> getDATA_PLAYER_MAIN_HAND() { return null; }
}
