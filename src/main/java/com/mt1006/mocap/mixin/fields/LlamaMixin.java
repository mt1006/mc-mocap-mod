package com.mt1006.mocap.mixin.fields;

import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Llama.class)
public interface LlamaMixin
{
	@Invoker void callSetSwag(@Nullable DyeColor dyeColor);
}
