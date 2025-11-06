package net.mt1006.mocap.mixin.fields;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Avatar.class)
public interface AvatarMixin
{
	@Accessor static EntityDataAccessor<Byte> getDATA_PLAYER_MODE_CUSTOMISATION() { return null; }
}
