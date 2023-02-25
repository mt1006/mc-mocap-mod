package com.mt1006.mocap.utils;

import com.google.common.collect.ImmutableList;
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
	private final @Nullable SynchedEntityData.DataValue<T> dataValue;

	public EntityData(Entity entity, DataIndex<T> index, T value)
	{
		this.entity = entity;
		this.dataValue = index.asDataValue(value);
	}

	public void broadcastAll(PlayerList packetTargets)
	{
		if (dataValue == null) { return; }
		packetTargets.broadcastAll(new ClientboundSetEntityDataPacket(entity.getId(), ImmutableList.of(dataValue)));
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

		public @Nullable SynchedEntityData.DataValue<T> asDataValue(T value)
		{
			if (!initialized) { return null; }
			return new SynchedEntityData.DataValue<>(index, serializer, value);
		}
	}
}
