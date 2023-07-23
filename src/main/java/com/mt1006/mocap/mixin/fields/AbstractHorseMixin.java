package com.mt1006.mocap.mixin.fields;

import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.datasync.DataParameter;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractHorseEntity.class)
public interface AbstractHorseMixin
{
	@Accessor static @Nullable DataParameter<Byte> getDATA_ID_FLAGS() { return null; }
	@Accessor Inventory getInventory();
}
