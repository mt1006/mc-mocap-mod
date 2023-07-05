package com.mt1006.mocap.mixin.fields;

import net.minecraft.entity.item.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BoatEntity.class)
public interface BoatMixin
{
	@Invoker void callSetBubbleTime(int val);
	@Invoker int callGetBubbleTime();
}
