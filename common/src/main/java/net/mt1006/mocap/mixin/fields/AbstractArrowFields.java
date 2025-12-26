package net.mt1006.mocap.mixin.fields;

import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractArrow.class)
public interface AbstractArrowFields
{
	@Accessor boolean getInGround();
	@Accessor void setInGround(boolean val);
}
