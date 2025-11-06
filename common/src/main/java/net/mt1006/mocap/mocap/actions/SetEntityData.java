package net.mt1006.mocap.mocap.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.coppergolem.CopperGolemState;
import net.minecraft.world.level.block.state.BlockState;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.mixin.fields.CopperGolemStateFields;
import net.mt1006.mocap.mixin.fields.EntityFields;
import net.mt1006.mocap.mixin.fields.SynchedEntityDataFields;
import net.mt1006.mocap.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SetEntityData implements MocapAction
{
	private final byte id;
	private final byte dataType;
	private final Object dataVal;

	public static SetEntityData fromReader(Reader reader, MocapRecordingData recordingData)
	{
		byte id = reader.readByte();
		byte dataType = reader.readByte();
		Object dataVal = readDataValue(reader, recordingData, dataType);

		return dataVal != null ? new SetEntityData(id, dataType, dataVal) : new SetEntityData(id, (byte)0, null);
	}

	public static List<SetEntityData> getDirtyActions(Entity entity)
	{
		SynchedEntityData entityData = ((EntityFields)entity).getEntityData();
		if (!((DirtyDataContainer)entityData).mocap$isDirty()) { return List.of(); }

		List<SetEntityData> actions = new ArrayList<>();

		for (SynchedEntityData.DataItem<?> dataItem : ((SynchedEntityDataFields)entityData).getItemsById())
		{
			if (((DirtyDataContainer)dataItem).mocap$isDirty())
			{
				SetEntityData action = SetEntityData.fromDataValue(dataItem.value());
				if (action != null) { actions.add(action); }
				((DirtyDataContainer)dataItem).mocap$clearDirty();
			}
		}

		((DirtyDataContainer)entityData).mocap$clearDirty();
		return actions;
	}

	private static @Nullable SetEntityData fromDataValue(SynchedEntityData.DataValue<?> dataValue)
	{
		byte id = (byte)dataValue.id();
		byte dataType = dataTypeByValue(dataValue);
		if (dataType == 0) { return null; }

		Object simpleDataVal = convertValueToSimple(dataType, dataValue.value());
		return simpleDataVal != null ? new SetEntityData(id, dataType, simpleDataVal) : null;
	}

	private static byte dataTypeByValue(SynchedEntityData.DataValue<?> dataValue)
	{
		byte type = switch (dataValue.value())
		{
			case Byte ignore -> 1;
			case Short ignore -> 2;
			case Integer ignore -> 3;
			case Long ignore -> 4;
			case Float ignore -> 5;
			case Double ignore -> 6;
			case Boolean ignore -> 7;
			case String ignore -> 8;
			case Component ignore -> 9; // converted to String
			case ParticleOptions ignore -> 10;  // converted to String
			case BlockState ignore -> 11;  // converted to BlockStateData
			case Rotations ignore -> 12;
			case BlockPos ignore -> 13;
			case Direction ignore -> 14;
			case OptionalInt ignore -> 15;
			case Pose ignore -> 16;
			case CopperGolemState ignore -> 17;
			default -> 0;
		};

		if (type == 0)
		{
			EntityDataSerializer<?> serializer = dataValue.serializer();
			if (serializer == EntityDataSerializers.OPTIONAL_BLOCK_POS) { return 18; }
			if (serializer == EntityDataSerializers.OPTIONAL_BLOCK_STATE) { return 19; } // converted to Optional<BlockStateData>
			if (serializer == EntityDataSerializers.OPTIONAL_COMPONENT) { return 20; } // converted to Optional<String>
			if (serializer == EntityDataSerializers.PARTICLES) { return 21; } //  // converted to List<String>
		}
		return type;
	}

	private static @Nullable Object convertValueToSimple(byte dataType, Object complexVal)
	{
		try
		{
			Object simpleVal = switch (dataType)
			{
				case 9 -> Utils.encodeToJsonStr(ComponentSerialization.CODEC, (Component)complexVal);
				case 10 -> Utils.encodeToJsonStr(ParticleTypes.CODEC, (ParticleOptions)complexVal);
				case 11 -> new BlockStateData((BlockState)complexVal);
				case 19 -> ((Optional<BlockState>)complexVal).map(BlockStateData::new);
				case 20 -> ((Optional<Component>)complexVal).map((val) -> Utils.encodeToJsonStr(ComponentSerialization.CODEC, val));
				default -> null;
			};

			if (dataType == 21)
			{
				List<String> particleDataList = new ArrayList<>();
				((List<ParticleOptions>)complexVal).forEach((p) -> particleDataList.add(Utils.encodeToJsonStr(ParticleTypes.CODEC, p)));
				simpleVal = particleDataList;
			}

			return simpleVal != null ? simpleVal : complexVal;
		}
		catch (IllegalStateException e) { return null; }
	}

	private static @Nullable Object convertValueToComplex(byte dataType, Object simpleVal)
	{
		try
		{
			Object complexVal = switch (dataType)
			{
				case 9 -> Utils.decodeFromJsonStr(ComponentSerialization.CODEC, (String)simpleVal);
				case 10 -> Utils.decodeFromJsonStr(ParticleTypes.CODEC, (String)simpleVal);
				case 11 -> ((BlockStateData)simpleVal).get();
				case 19 -> ((Optional<BlockStateData>)simpleVal).map(BlockStateData::get);
				case 20 -> ((Optional<String>)simpleVal).map((val) -> Utils.decodeFromJsonStr(ComponentSerialization.CODEC, val));
				default -> null;
			};

			if (dataType == 21)
			{
				List<ParticleOptions> particleDataList = new ArrayList<>();
				((List<String>)simpleVal).forEach((p) -> particleDataList.add(Utils.decodeFromJsonStr(ParticleTypes.CODEC, p)));
				complexVal = particleDataList;
			}

			return complexVal != null ? complexVal : simpleVal;
		}
		catch (IllegalStateException e) { return null; }
	}

	private static void writeDataValue(Writer writer, byte dataType, Object val)
	{
		switch (dataType)
		{
			case 1: writer.addByte((Byte)val); break;
			case 2: writer.addShort((Short)val); break;
			case 3: writer.addPackedInt((Integer)val); break;
			case 4: writer.addLong((Long)val); break;
			case 5: writer.addFloat((Float)val); break;
			case 6: writer.addDouble((Double)val); break;
			case 7: writer.addBoolean((Boolean)val); break;
			case 8, 9, 10: writer.addString((String)val); break; // string or objects packed into JSON string
			case 11: ((BlockStateData)val).write(writer); break;

			case 12:
				writer.addFloat(((Rotations)val).x());
				writer.addFloat(((Rotations)val).y());
				writer.addFloat(((Rotations)val).z());
				break;

			case 13: writer.addBlockPos((BlockPos)val); break;
			case 14: writer.addPackedInt(((Direction)val).get3DDataValue()); break;

			case 15:
				writer.addBoolean(((OptionalInt)val).isPresent());
				if (((OptionalInt)val).isPresent()) { writer.addPackedInt(((OptionalInt)val).getAsInt()); }
				break;

			case 16: writer.addPackedInt(((Pose)val).id()); break;
			case 17: writer.addPackedInt(((CopperGolemStateFields)val).callId()); break;
			case 18: writeOptional(writer, (Optional<BlockPos>)val, writer::addBlockPos); break;
			case 19: writeOptional(writer, (Optional<BlockStateData>)val, (optVal) -> optVal.write(writer)); break;
			case 20: writeOptional(writer, (Optional<String>)val, writer::addString); break;

			case 21:
				writer.addPackedInt(((List<String>)val).size());
				((List<String>)val).forEach(writer::addString);
				break;
		}
	}

	private static <T> void writeOptional(Writer writer, Optional<T> opt, Consumer<T> writeFunc)
	{
		writer.addBoolean(opt.isPresent());
		opt.ifPresent(writeFunc);
	}

	private static @Nullable Object readDataValue(Reader reader, MocapRecordingData recordingData, byte dataType)
	{
		switch (dataType)
		{
			case 1: return reader.readByte();
			case 2: return reader.readShort();
			case 3: return reader.readPackedInt();
			case 4: return reader.readLong();
			case 5: return reader.readFloat();
			case 6: return reader.readDouble();
			case 7: return reader.readBoolean();
			case 8, 9, 10: return reader.readString(); // string or objects packed into JSON string
			case 11: return new BlockStateData(reader, recordingData);
			case 12: return new Rotations(reader.readFloat(), reader.readFloat(), reader.readFloat());
			case 13: return reader.readBlockPos();
			case 14: return Direction.from3DDataValue(reader.readPackedInt());

			case 15:
				boolean present = reader.readBoolean();
				return present ? OptionalInt.of(reader.readPackedInt()) : OptionalInt.empty();

			case 16: return Pose.BY_ID.apply(reader.readPackedInt());
			case 17: return CopperGolemStateFields.getBY_ID().apply(reader.readPackedInt());
			case 18: return readOptional(reader, reader::readBlockPos);
			case 19: return readOptional(reader, () -> new BlockStateData(reader, recordingData));
			case 20: return readOptional(reader, reader::readString);

			case 21:
				int size = reader.readPackedInt();
				List<String> particleDataList = new ArrayList<>();
				for (int i = 0; i < size; i++)
				{
					particleDataList.add(reader.readString());
				}
				return particleDataList;

			default: return null;
		}
	}

	private static <T> Optional<T> readOptional(Reader reader, Supplier<T> readFunc)
	{
		boolean present = reader.readBoolean();
		return present ? Optional.of(readFunc.get()) : Optional.empty();
	}

	private SetEntityData(byte id, byte dataType, Object dataVal)
	{
		this.id = id;
		this.dataType = dataType;
		this.dataVal = dataVal;
	}

	@Override public void prepareWrite(MocapRecordingData recordingData)
	{
		if (dataType == 11)
		{
			((BlockStateData)dataVal).prepareWrite(recordingData);
		}
		else if (dataType == 19)
		{
			if (((Optional<?>)dataVal).isPresent()) { ((Optional<BlockStateData>)dataVal).get().prepareWrite(recordingData); }
		}
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addByte(id);
		writer.addByte(dataType);
		writeDataValue(writer, dataType, dataVal);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (dataType == 0) { return Result.IGNORED; }

		SynchedEntityData entityData = ((EntityFields)ctx.getEntity()).getEntityData();
		SynchedEntityDataFields entityDataFields = (SynchedEntityDataFields)entityData;

		//TODO: verify id in range?
		SynchedEntityData.DataItem<Object> dataItem = (SynchedEntityData.DataItem<Object>)entityDataFields.getItemsById()[id];

		Object complexDataVal = convertValueToComplex(dataType, dataVal);
		if (complexDataVal == null) { return Result.IGNORED; }

		entityData.set(dataItem.getAccessor(), complexDataVal);
		MocapMod.LOGGER.warn("{} {}", dataItem.getAccessor().id(), complexDataVal); //TODO: remove
		return Result.OK;
	}

	public interface DirtyDataContainer
	{
		boolean mocap$isDirty();
		void mocap$clearDirty();
	}
}
