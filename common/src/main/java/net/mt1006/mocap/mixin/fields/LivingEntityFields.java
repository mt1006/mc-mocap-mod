package net.mt1006.mocap.mixin.fields;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(LivingEntity.class)
public interface LivingEntityFields
{
	@Accessor static EntityDataAccessor<Byte> getDATA_LIVING_ENTITY_FLAGS() { return null; }
	@Accessor static EntityDataAccessor<List<ParticleOptions>> getDATA_EFFECT_PARTICLES() { return null; }
	@Accessor static EntityDataAccessor<Boolean> getDATA_EFFECT_AMBIENCE_ID() { return null; }
	@Invoker void callDetectEquipmentUpdates();
}
