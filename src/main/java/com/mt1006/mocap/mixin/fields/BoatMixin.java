package com.mt1006.mocap.mixin.fields;

import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Boat.class)
public interface BoatMixin
{
	@Invoker void callSetBubbleTime(int val);
	@Invoker int callGetBubbleTime();
}
