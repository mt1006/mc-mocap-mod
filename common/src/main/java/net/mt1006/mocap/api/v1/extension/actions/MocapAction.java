package net.mt1006.mocap.api.v1.extension.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface MocapAction
{
	default void prepareWrite(MocapRecordingData data) {}

	void write(Writer writer, MocapRecordingData data);

	Result execute(MocapActionContext ctx);

	interface FromReader extends BiFunction<Reader, MocapRecordingData, MocapAction> {}
	interface FromReaderOnly extends Function<Reader, MocapAction> {}
	interface FromEntity extends Function<Entity, MocapStateAction> {}

	interface Writer
	{
		void addByte(byte val);
		void addShort(short val);
		void addInt(int val);
		void addFloat(float val);
		void addDouble(double val);
		void addBoolean(boolean val);
		void addString(String val);
		void addVec3(Vec3 vec);
		void addBlockPos(BlockPos blockPos);
		void addPackedSize(int size);
	}

	interface Reader
	{
		byte readByte();
		short readShort();
		int readInt();
		float readFloat();
		double readDouble();
		boolean readBoolean();
		String readString();
		Vec3 readVec3();
		BlockPos readBlockPos();
		int readPackedSize();
		void shift(int val);
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
