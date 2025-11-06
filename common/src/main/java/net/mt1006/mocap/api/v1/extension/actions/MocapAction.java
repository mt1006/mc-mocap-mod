package net.mt1006.mocap.api.v1.extension.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface MocapAction
{
	default void prepareWrite(MocapRecordingData data) {}

	void write(Writer writer, MocapRecordingData data);

	Result execute(MocapActionContext ctx);

	interface FromReader extends BiFunction<Reader, MocapRecordingData, MocapAction> {}
	interface FromReaderOnly extends Function<Reader, MocapAction> {}
	interface FromEntity extends Function<Entity, @Nullable MocapStateAction> {}

	interface Writer
	{
		void addByte(byte val);
		void addShort(short val);
		void addInt(int val);
		void addLong(long val);
		void addFloat(float val);
		void addDouble(double val);
		void addBoolean(boolean val);
		void addByteArray(byte[] arr);
		void addString(String val);
		void addUUID(UUID val);
		void addVec3(Vec3 vec);
		void addBlockPos(BlockPos blockPos);
		void addPackedInt(int val);
	}

	interface Reader
	{
		byte readByte();
		short readShort();
		int readInt();
		long readLong();
		float readFloat();
		double readDouble();
		boolean readBoolean();
		String readString();
		byte[] readByteArray(int size);
		UUID readUUID();
		Vec3 readVec3();
		BlockPos readBlockPos();
		int readPackedInt();
		void shift(int val);
		boolean isDummy();
	}

	enum Result
	{
		OK,
		IGNORED,
		NEXT_TICK,
		REPEAT_TICK,
		END,
		ERROR
	}
}
