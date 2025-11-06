package net.mt1006.mocap.mixin;

import net.minecraft.network.syncher.SynchedEntityData;
import net.mt1006.mocap.mocap.actions.SetEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SynchedEntityData.DataItem.class)
public class SynchedEntityDataItemMixin implements SetEntityData.DirtyDataContainer
{
	@Unique private boolean mocap$isDirty = true;

	@Inject(method = "setDirty", at = @At(value = "HEAD"))
	public void atSetDirty(boolean dirty, CallbackInfo ci)
	{
		if (dirty) { mocap$isDirty = true; }
	}

	@Override public boolean mocap$isDirty()
	{
		return mocap$isDirty;
	}

	@Override public void mocap$clearDirty()
	{
		mocap$isDirty = false;
	}
}
