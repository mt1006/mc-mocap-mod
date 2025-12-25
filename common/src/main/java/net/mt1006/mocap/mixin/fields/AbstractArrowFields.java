package net.mt1006.mocap.mixin.fields;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractArrow.class)
public interface AbstractArrowFields
{
	@Invoker boolean callIsInGround();
	@Invoker void callSetInGround(boolean val);
}
