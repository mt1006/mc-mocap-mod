package net.mt1006.mocap.mixin.fields;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityFields
{
	@Accessor SynchedEntityData getEntityData();
	@Accessor static EntityDataAccessor<Byte> getDATA_SHARED_FLAGS_ID() { return null; }
	@Invoker void callUnsetRemoved();
}
