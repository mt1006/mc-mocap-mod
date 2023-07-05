package com.mt1006.mocap.mixin.fields;

import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.item.DyeColor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LlamaEntity.class)
public interface LlamaMixin
{
	@Invoker void callSetSwag(@Nullable DyeColor dyeColor);
}
