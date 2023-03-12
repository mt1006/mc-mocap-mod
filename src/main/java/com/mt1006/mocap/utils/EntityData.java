package com.mt1006.mocap.utils;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.mixin.EntityMixin;
import com.mt1006.mocap.mixin.LivingEntityMixin;
import com.mt1006.mocap.mixin.PlayerMixin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Pose;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SEntityMetadataPacket;
import net.minecraft.server.management.PlayerList;
import org.jetbrains.annotations.Nullable;

public class EntityData<T>
{
	public static final DataIndex<Byte> ENTITY_FLAGS =         new DataIndex<>(EntityMixin.getDATA_SHARED_FLAGS_ID());
	public static final DataIndex<Pose> POSE =                 new DataIndex<>(EntityMixin.getDATA_POSE());
	public static final DataIndex<Byte> LIVING_ENTITY_FLAGS =  new DataIndex<>(LivingEntityMixin.getDATA_LIVING_ENTITY_FLAGS());
	public static final DataIndex<Byte> SET_SKIN_PARTS =       new DataIndex<>(PlayerMixin.getDATA_PLAYER_MODE_CUSTOMISATION());
	public static final DataIndex<Byte> SET_MAIN_HAND =        new DataIndex<>(PlayerMixin.getDATA_PLAYER_MAIN_HAND());
	private final Entity entity;
	private final EntityDataManager dataManager;

	public EntityData(Entity entity, DataIndex<T> index, T value)
	{
		this.entity = entity;
		this.dataManager = index.asDataValue(entity, value);
	}

	public void broadcastAll(PlayerList packetTargets)
	{
		if (dataManager == null) { return; }
		packetTargets.broadcastAll(new SEntityMetadataPacket(entity.getId(), dataManager, true));
	}

	public static class DataIndex<T>
	{
		private DataParameter<T> parameter;
		private boolean initialized = false;

		public DataIndex(@Nullable DataParameter<T> accessor)
		{
			if (accessor == null)
			{
				MocapMod.LOGGER.error("Failed to initialize one of the data indexes!");
				return;
			}

			parameter = accessor;
			initialized = true;
		}

		public @Nullable EntityDataManager asDataValue(Entity entity, T value)
		{
			if (!initialized) { return null; }

			EntityDataManager dataManager = new EntityDataManager(entity);
			dataManager.define(parameter, value);
			return dataManager;
		}
	}
}
