package com.mt1006.mocap.utils;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.mixin.fields.*;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Pose;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.network.play.server.SEntityMetadataPacket;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;

public class EntityData
{
	public static final DataIndex<Byte> ENTITY_FLAGS =                          new DataIndex<>(EntityMixin.getDATA_SHARED_FLAGS_ID());
	public static final DataIndex<Pose> ENTITY_POSE =                           new DataIndex<>(EntityMixin.getDATA_POSE());
	public static final DataIndex<Byte> LIVING_ENTITY_FLAGS =                   new DataIndex<>(LivingEntityMixin.getDATA_LIVING_ENTITY_FLAGS());
	public static final DataIndex<Integer> LIVING_ENTITY_EFFECT_COLOR =         new DataIndex<>(LivingEntityMixin.getDATA_EFFECT_COLOR_ID());
	public static final DataIndex<Boolean> LIVING_ENTITY_EFFECT_AMBIENCE =      new DataIndex<>(LivingEntityMixin.getDATA_EFFECT_AMBIENCE_ID());
	public static final DataIndex<Integer> LIVING_ENTITY_ARROW_COUNT =          new DataIndex<>(LivingEntityMixin.getDATA_ARROW_COUNT_ID());
	public static final DataIndex<Integer> LIVING_ENTITY_STINGER_COUNT =        new DataIndex<>(LivingEntityMixin.getDATA_STINGER_COUNT_ID());
	public static final DataIndex<Optional<BlockPos>> LIVING_ENTITY_BED_POS =   new DataIndex<>(LivingEntityMixin.getSLEEPING_POS_ID());
	public static final DataIndex<Byte> PLAYER_SKIN_PARTS =                     new DataIndex<>(PlayerMixin.getDATA_PLAYER_MODE_CUSTOMISATION());
	public static final DataIndex<Byte> PLAYER_MAIN_HAND =                      new DataIndex<>(PlayerMixin.getDATA_PLAYER_MAIN_HAND());
	public static final DataIndex<Byte> ABSTRACT_HORSE_FLAGS =                  new DataIndex<>(AbstractHorseMixin.getDATA_ID_FLAGS());
	public static final DataIndex<Boolean> PIG_HAS_SADDLE =                     new DataIndex<>(PigMixin.getDATA_SADDLE_ID());
	private final Entity entity;
	private final ArrayList<EntityDataManager> dataValues = new ArrayList<>();

	public EntityData(Entity entity)
	{
		this.entity = entity;
	}

	public <T> EntityData(Entity entity, DataIndex<T> index, T value)
	{
		this.entity = entity;
		add(index, value);
	}

	public <T> void add(DataIndex<T> index, T value)
	{
		EntityDataManager dataValue = index.asDataValue(entity, value);
		if (dataValue != null) { dataValues.add(dataValue); }
	}

	public void broadcast(PlayerList packetTargets)
	{
		if (dataValues.size() == 0) { return; }
		for (EntityDataManager dataValue : dataValues)
		{
			packetTargets.broadcastAll(new SEntityMetadataPacket(entity.getId(), dataValue, true));
		}
	}

	public void broadcast(PlayingContext ctx)
	{
		if (dataValues.size() == 0) { return; }
		for (EntityDataManager dataValue : dataValues)
		{
			ctx.packetTargets.broadcastAll(new SEntityMetadataPacket(entity.getId(), dataValue, true));
		}
	}

	public static class DataIndex<T>
	{
		private int index;
		private IDataSerializer<T> serializer;
		private @Nullable DataParameter<T> accessor = null;
		private boolean initialized = false;

		public DataIndex(@Nullable DataParameter<T> accessor)
		{
			if (accessor == null)
			{
				MocapMod.LOGGER.error("Failed to initialize one of the data indexes!");
				return;
			}

			this.index = accessor.getId();
			this.serializer = accessor.getSerializer();
			this.accessor = accessor;
			this.initialized = true;
		}

		public @Nullable EntityDataManager asDataValue(Entity entity, T value)
		{
			if (!initialized) { return null; }

			EntityDataManager dataManager = new EntityDataManager(entity);
			dataManager.define(new DataParameter<>(index, serializer), value);
			return dataManager;
		}

		public T valOrDef(Entity entity, T defVal)
		{
			return accessor != null ? entity.getEntityData().get(accessor) : defVal;
		}
	}
}
