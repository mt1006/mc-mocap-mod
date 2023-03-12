package com.mt1006.mocap.utils;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.mixin.EntityMixin;
import com.mt1006.mocap.mixin.LivingEntityMixin;
import com.mt1006.mocap.mixin.PlayerMixin;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.Nullable;

public class EntityData<T>
{
	public static final DataIndex<Byte> ENTITY_FLAGS =         new DataIndex<>(EntityMixin.getDATA_SHARED_FLAGS_ID());
	public static final DataIndex<Pose> POSE =                 new DataIndex<>(EntityMixin.getDATA_POSE());
	public static final DataIndex<Byte> LIVING_ENTITY_FLAGS =  new DataIndex<>(LivingEntityMixin.getDATA_LIVING_ENTITY_FLAGS());
	public static final DataIndex<Byte> SET_SKIN_PARTS =       new DataIndex<>(PlayerMixin.getDATA_PLAYER_MODE_CUSTOMISATION());
	public static final DataIndex<Byte> SET_MAIN_HAND =        new DataIndex<>(PlayerMixin.getDATA_PLAYER_MAIN_HAND());
	private final Entity entity;
	private final SynchedEntityData dataManager;

	public EntityData(Entity entity, DataIndex<T> index, T value)
	{
		this.entity = entity;
		this.dataManager = index.asDataValue(entity, value);
	}

	public void broadcastAll(PlayerList packetTargets)
	{
		if (dataManager == null) { return; }
		packetTargets.broadcastAll(new ClientboundSetEntityDataPacket(entity.getId(), dataManager, true));
	}

	public static class DataIndex<T>
	{
		private int index;
		private EntityDataSerializer<T> serializer;
		private boolean initialized = false;

		public DataIndex(@Nullable EntityDataAccessor<T> accessor)
		{
			if (accessor == null)
			{
				MocapMod.LOGGER.error("Failed to initialize one of the data indexes!");
				return;
			}

			index = accessor.getId();
			serializer = accessor.getSerializer();
			initialized = true;
		}

		public @Nullable SynchedEntityData asDataValue(Entity entity, T value)
		{
			if (!initialized) { return null; }

			SynchedEntityData dataManager = new SynchedEntityData(entity);
			dataManager.define(new EntityDataAccessor<>(index, serializer), value);
			return dataManager;
		}
	}
}
