package com.mt1006.mocap.mixin.fields;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Player.class)
public interface PlayerMixin
{
	@Accessor static @Nullable EntityDataAccessor<Byte> getDATA_PLAYER_MODE_CUSTOMISATION() { return null; }
	@Accessor static @Nullable EntityDataAccessor<Byte> getDATA_PLAYER_MAIN_HAND() { return null; }
}
