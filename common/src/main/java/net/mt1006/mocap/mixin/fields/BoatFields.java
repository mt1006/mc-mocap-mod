package net.mt1006.mocap.mixin.fields;

import net.minecraft.world.entity.vehicle.AbstractBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractBoat.class)
public interface BoatFields
{
	@Invoker void callSetBubbleTime(int val);
	@Invoker int callGetBubbleTime();
}
