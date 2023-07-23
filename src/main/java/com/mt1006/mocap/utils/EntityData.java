package com.mt1006.mocap.utils;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.mixin.fields.AbstractHorseMixin;
import com.mt1006.mocap.mixin.fields.EntityMixin;
import com.mt1006.mocap.mixin.fields.LivingEntityMixin;
import com.mt1006.mocap.mixin.fields.PlayerMixin;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import org.jetbrains.annotations.Nullable;

public class EntityData
{
	public static final DataIndex<Byte> ENTITY_FLAGS =                       new DataIndex<>(EntityMixin.getDATA_SHARED_FLAGS_ID());
	public static final DataIndex<Byte> LIVING_ENTITY_FLAGS =                new DataIndex<>(LivingEntityMixin.getDATA_LIVING_ENTITY_FLAGS());
	public static final DataIndex<Integer> LIVING_ENTITY_EFFECT_COLOR =      new DataIndex<>(LivingEntityMixin.getDATA_EFFECT_COLOR_ID());
	public static final DataIndex<Boolean> LIVING_ENTITY_EFFECT_AMBIENCE =   new DataIndex<>(LivingEntityMixin.getDATA_EFFECT_AMBIENCE_ID());
	public static final DataIndex<Byte> PLAYER_SKIN_PARTS =                  new DataIndex<>(PlayerMixin.getDATA_PLAYER_MODE_CUSTOMISATION());
	public static final DataIndex<Byte> ABSTRACT_HORSE_FLAGS =               new DataIndex<>(AbstractHorseMixin.getDATA_ID_FLAGS());

	public static class DataIndex<T>
	{
		private final @Nullable DataParameter<T> accessor;

		public DataIndex(@Nullable DataParameter<T> accessor)
		{
			this.accessor = accessor;
			if (accessor == null) { MocapMod.LOGGER.error("Failed to initialize one of the data indexes!"); }
		}

		public void set(Entity entity, T val)
		{
			if (accessor != null) { entity.getEntityData().set(accessor, val); }
		}

		public T valOrDef(Entity entity, T defVal)
		{
			return accessor != null ? entity.getEntityData().get(accessor) : defVal;
		}
	}
}
