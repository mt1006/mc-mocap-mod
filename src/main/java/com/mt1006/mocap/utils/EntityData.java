package com.mt1006.mocap.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Pose;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.network.play.server.SEntityMetadataPacket;

public class EntityData<T>
{
	// 1.16.5
	// https://wiki.vg/index.php?title=Entity_metadata&oldid=16348

	public static final DataIndex<Byte> ENTITY_FLAGS =         new DataIndex<>(0,  DataSerializers.BYTE);   // Entity
	public static final DataIndex<Pose> POSE =                 new DataIndex<>(6,  DataSerializers.POSE);   // Entity
	public static final DataIndex<Byte> LIVING_ENTITY_FLAGS =  new DataIndex<>(7,  DataSerializers.BYTE);   // Living Entity
	public static final DataIndex<Byte> SET_SKIN_PARTS =       new DataIndex<>(16, DataSerializers.BYTE);   // Player
	public static final DataIndex<Byte> SET_MAIN_HAND =        new DataIndex<>(17, DataSerializers.BYTE);   // Player


	/*
	// 1.18.2 - 1.19.3
	// https://wiki.vg/index.php?title=Entity_metadata&oldid=17909

	public static final DataIndex<Byte> ENTITY_FLAGS =         new DataIndex<>(0,  DataSerializers.BYTE);   // Entity
	public static final DataIndex<Pose> POSE =                 new DataIndex<>(6,  DataSerializers.POSE);   // Entity
	public static final DataIndex<Byte> LIVING_ENTITY_FLAGS =  new DataIndex<>(8,  DataSerializers.BYTE);   // Living Entity
	public static final DataIndex<Byte> SET_SKIN_PARTS =       new DataIndex<>(17, DataSerializers.BYTE);   // Player
	public static final DataIndex<Byte> SET_MAIN_HAND =        new DataIndex<>(18, DataSerializers.BYTE);   // Player
	*/


	private final Entity entity;
	private final EntityDataManager dataManager;

	public EntityData(Entity entity, DataIndex<T> index, T value)
	{
		this.entity = entity;
		this.dataManager = new EntityDataManager(entity);

		DataParameter<T> dataAccessor = new DataParameter<>(index.index, index.serializer);
		dataManager.define(dataAccessor, value);
	}

	public SEntityMetadataPacket getPacket()
	{
		return new SEntityMetadataPacket(entity.getId(), dataManager, true);
	}

	public static class DataIndex<T>
	{
		public int index;
		public IDataSerializer<T> serializer;

		public DataIndex(int index, IDataSerializer<T> serializer)
		{
			this.index = index;
			this.serializer = serializer;
		}
	}
}
