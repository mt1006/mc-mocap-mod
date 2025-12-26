package net.mt1006.mocap.mixin.fields;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayer.class)
public interface ServerPlayerFields
{
	@Accessor void setSpawnInvulnerableTime(int val);
}
