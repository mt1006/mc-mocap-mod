package com.mt1006.mocap.utils;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;

public class EntityData<T>
{
	/*
	// 1.16.5
	// https://wiki.vg/index.php?title=Entity_metadata&oldid=16348

	public static final DataIndex<Byte> ENTITY_FLAGS =         new DataIndex<>(0,  EntityDataSerializers.BYTE);   // Entity
	public static final DataIndex<Pose> POSE =                 new DataIndex<>(6,  EntityDataSerializers.POSE);   // Entity
	public static final DataIndex<Byte> LIVING_ENTITY_FLAGS =  new DataIndex<>(7,  EntityDataSerializers.BYTE);   // Living Entity
	public static final DataIndex<Byte> SET_SKIN_PARTS =       new DataIndex<>(16, EntityDataSerializers.BYTE);   // Player
	public static final DataIndex<Byte> SET_MAIN_HAND =        new DataIndex<>(17, EntityDataSerializers.BYTE);   // Player
	*/


	// 1.18.2 - 1.19.3
	// https://wiki.vg/index.php?title=Entity_metadata&oldid=17909

	public static final DataIndex<Byte> ENTITY_FLAGS =         new DataIndex<>(0,  EntityDataSerializers.BYTE);   // Entity
	public static final DataIndex<Pose> POSE =                 new DataIndex<>(6,  EntityDataSerializers.POSE);   // Entity
	public static final DataIndex<Byte> LIVING_ENTITY_FLAGS =  new DataIndex<>(8,  EntityDataSerializers.BYTE);   // Living Entity
	public static final DataIndex<Byte> SET_SKIN_PARTS =       new DataIndex<>(17, EntityDataSerializers.BYTE);   // Player
	public static final DataIndex<Byte> SET_MAIN_HAND =        new DataIndex<>(18, EntityDataSerializers.BYTE);   // Player


	private final Entity entity;
	private final SynchedEntityData.DataValue<T> dataValue;

	public EntityData(Entity entity, DataIndex<T> index, T value)
	{
		this.entity = entity;
		this.dataValue = new SynchedEntityData.DataValue<>(index.index, index.serializer, value);
	}

	public ClientboundSetEntityDataPacket getPacket()
	{
		return new ClientboundSetEntityDataPacket(entity.getId(), ImmutableList.of(dataValue));
	}

	public static class DataIndex<T>
	{
		public int index;
		public EntityDataSerializer<T> serializer;

		public DataIndex(int index, EntityDataSerializer<T> serializer)
		{
			this.index = index;
			this.serializer = serializer;
		}
	}
}
