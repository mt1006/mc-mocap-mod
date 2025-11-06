package net.mt1006.mocap.mixin.fields;

import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SynchedEntityData.class)
public interface SynchedEntityDataFields
{
	@Accessor @Final SynchedEntityData.DataItem<?>[] getItemsById();
}
