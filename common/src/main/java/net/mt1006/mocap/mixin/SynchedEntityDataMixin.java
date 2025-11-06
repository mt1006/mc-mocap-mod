package net.mt1006.mocap.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.mt1006.mocap.mocap.actions.SetEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SynchedEntityData.class)
public class SynchedEntityDataMixin implements SetEntityData.DirtyDataContainer
{
	private boolean mocap$isDirty = true;

	@Inject(method = "set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;Z)V", at = @At(value = "INVOKE", target = "setValue"))
	public <T> void atSet(EntityDataAccessor<T> key, T value, boolean force, CallbackInfo ci)
	{
		mocap$isDirty = true;
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
