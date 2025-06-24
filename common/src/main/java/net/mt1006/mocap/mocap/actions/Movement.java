package net.mt1006.mocap.mocap.actions;

import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.mixin.fields.EntityFields;
import org.jetbrains.annotations.Nullable;

public class Movement implements MocapAction
{
	/*
	Flags:
	0b------xx:
	  00 - dy=0
	  01 - y*2 as short (or packed dy, see 0bx-------)
	  10 - dy as float
	  11 - y as double
	0b----xx--:
	  00 - dx=0, dz=0
	  01 - dx/PACKED_XZ_DIV and dz/PACKED_XZ_DIV packed to short
	  10 - dx and dz as float
	  11 - x and z as double
	0b--xx----:
	  00 - dxRot=0, dyRot=0, dHeadRot=0
	  01 - xRot and yRot as short, headRot=0
	  10 - xRot and yRot as short, headRot=yRot
	  11 - xRot, yRot and headRot as short
	0b-x------: is on ground
	0bx-------: dy/PACKED_Y_DIV packed to short (first value; requires (flags & Y_MASK == Y_SHORT), otherwise not used)
	 */

	private static final byte Y_0 =            0b00000000;
	private static final byte Y_SHORT =        0b00000001;
	private static final byte Y_FLOAT =        0b00000010;
	private static final byte Y_DOUBLE =       0b00000011;
	private static final byte XZ_0 =           0b00000000;
	private static final byte XZ_SHORT =       0b00000100;
	private static final byte XZ_FLOAT =       0b00001000;
	private static final byte XZ_DOUBLE =      0b00001100;
	private static final byte ROT_0 =          0b00000000;
	private static final byte ROT_HEAD_0 =     0b00010000;
	private static final byte ROT_HEAD_EQ =    0b00100000;
	private static final byte ROT_HEAD_DIFF =  0b00110000;
	private static final byte ON_GROUND =      0b01000000;
	private static final byte PACKED_Y = (byte)0b10000000;

	private static final byte MASK_Y =      0b00000011;
	private static final byte MASK_XZ =     0b00001100;
	private static final byte MASK_ROT =    0b00110000;

	private static final double PACKED_Y_DIV = 4.0;   // 78.4/20 = 3.92 (max player falling speed)
	private static final double PACKED_XZ_DIV = 2.0;  // 33.5/20 = 1.675 (elytra with continuous rocket boost max speed)
	private static final double MAX_ERROR = 0.0002;   // 4/(2^15) = 0.000122 (packed Y error)   //TODO: make MAX_ERROR a setting?


	private final byte flags;
	private final Vec3 position;
	private final float[] rotation; // [0]=xRot, [1]=yRot
	private final float headRot;

	private Movement(byte flags, Vec3 position, float[] rotation, float headRot)
	{
		this.flags = flags;
		this.position = position;
		this.rotation = rotation;
		this.headRot = headRot;
	}

	public Movement(Reader reader)
	{
		flags = reader.readByte();

		double x, y, z;
		y = switch (flags & MASK_Y)
		{
			case Y_SHORT -> ((flags & PACKED_Y) != 0)
					? unpackValue(reader.readShort(), PACKED_Y_DIV)
					: ((double)reader.readShort() / 2.0);
			case Y_FLOAT -> reader.readFloat();
			case Y_DOUBLE -> reader.readDouble();
			default -> 0.0; // Y_0
		};

		x = readXZ(reader);
		z = readXZ(reader);
		position = new Vec3(x, y, z);

		rotation = new float[2];
		if ((flags & MASK_ROT) != ROT_0)
		{
			rotation[0] = unpackRot(reader.readShort());
			rotation[1] = unpackRot(reader.readShort());
		}

		headRot = switch (flags & MASK_ROT)
		{
			case ROT_HEAD_EQ -> rotation[1];
			case ROT_HEAD_DIFF -> unpackRot(reader.readShort());
			default -> 0.0f; // ROT_HEAD_0 or ROT_NO_DIFF
		};
	}

	private double readXZ(Reader reader)
	{
		return switch (flags & MASK_XZ)
		{
			case XZ_SHORT -> unpackValue(reader.readShort(), PACKED_XZ_DIV);
			case XZ_FLOAT -> reader.readFloat();
			case XZ_DOUBLE -> reader.readDouble();
			default -> 0.0; // XZ_0
		};
	}

	public static @Nullable Movement delta(double[] oldPos, Vec3 newPos, float[] oldRot, float newRotX, float newRotY,
										   float oldHeadRot, float newHeadRot, boolean oldOnGround, boolean onGround, boolean forceNonPosData)
	{
		byte flags = onGround ? ON_GROUND : 0;
		double x = 0.0, y = 0.0, z = 0.0;
		float[] rotation = new float[2];
		float headRot = 0.0f;

		double oldY = oldPos[1], newY = newPos.y;
		double deltaY = newY - oldY;
		if (Math.abs(deltaY) > MAX_ERROR)
		{
			double newY2 = newY * 2.0;
			if (newY2 == (short)newY2)
			{
				flags |= Y_SHORT;
				y = newY;
			}
			else if (canBePacked(oldY, newY, PACKED_Y_DIV))
			{
				flags |= Y_SHORT | PACKED_Y;
				y = deltaY;
			}
			else if (canUseDeltaFloat(oldY, newY))
			{
				flags |= Y_FLOAT;
				y = deltaY;
			}
			else
			{
				flags |= Y_DOUBLE;
				y = newY;
			}
		}

		double oldX = oldPos[0], newX = newPos.x;
		double oldZ = oldPos[2], newZ = newPos.z;
		double deltaX = newX - oldX;
		double deltaZ = newZ - oldZ;
		if (Math.abs(deltaX) > MAX_ERROR || Math.abs(deltaZ) > MAX_ERROR)
		{
			if (canBePacked(oldX, newX, PACKED_XZ_DIV) && canBePacked(oldZ, newZ, PACKED_XZ_DIV))
			{
				flags |= XZ_SHORT;
				x = deltaX;
				z = deltaZ;
			}
			else if (canUseDeltaFloat(oldX, newX) && canUseDeltaFloat(oldZ, newZ))
			{
				flags |= XZ_FLOAT;
				x = deltaX;
				z = deltaZ;
			}
			else
			{
				flags |= XZ_DOUBLE;
				x = newX;
				z = newZ;
			}
		}

		// unlike oldPos, oldRot and oldHeadRot should be actual old position, not calculated using previous Movement action
		// for this reason their difference is compared to 0, not MAX_ERROR
		if (newRotX - oldRot[0] != 0.0f || newRotY - oldRot[1] != 0.0f
				|| newHeadRot - oldHeadRot != 0.0f || forceNonPosData)
		{
			rotation[0] = newRotX;
			rotation[1] = newRotY;

			if (newHeadRot == 0.0f)
			{
				flags |= ROT_HEAD_0;
				headRot = 0.0f;
			}
			else if (newHeadRot == newRotY)
			{
				flags |= ROT_HEAD_EQ;
				headRot = newRotY;
			}
			else
			{
				flags |= ROT_HEAD_DIFF;
				headRot = newHeadRot;
			}
		}

		return ((flags & ~ON_GROUND) != 0 || oldOnGround != onGround || forceNonPosData)
				? new Movement(flags, new Vec3(x, y, z), rotation, headRot)
				: null;
	}

	public static Movement teleportToPos(Vec3 pos, boolean onGround)
	{
		byte flags = (byte)((onGround ? ON_GROUND : 0) | Y_DOUBLE | XZ_DOUBLE | ROT_0);

		float[] rotArray = new float[2];
		rotArray[0] = 0.0f;
		rotArray[1] = 0.0f;

		return new Movement(flags, pos, rotArray, 0.0f);
	}

	private static boolean canUseDeltaFloat(double oldVal, double newVal)
	{
		return Math.abs(oldVal + (double)(float)(newVal - oldVal) - newVal) <= MAX_ERROR;
	}

	private static boolean canBePacked(double oldVal, double newVal, double div)
	{
		double delta = newVal - oldVal;
		return delta <= div && delta >= -div && marginOfError(oldVal, newVal, unpackValue(packValue(delta, div), div));
	}

	private static short packValue(double delta, double div)
	{
		return (short)((delta / div) * (double)Short.MAX_VALUE);
	}

	private static double unpackValue(short packed, double div)
	{
		return ((double)packed / (double)Short.MAX_VALUE) * div;
	}

	private static boolean marginOfError(double oldVal, double expected, double delta)
	{
		return Math.abs(oldVal + delta - expected) <= MAX_ERROR;
	}

	private static short packRot(float rot)
	{
		return (short)(((double)rot / 360.0) * (double)0x10000);
	}

	private static float unpackRot(short packed)
	{
		return (float)(((double)packed / (double)0x10000) * 360.0);
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addByte(flags);

		switch (flags & MASK_Y)
		{
			case Y_SHORT -> writer.addShort(((flags & PACKED_Y) != 0)
					? packValue(position.y, PACKED_Y_DIV)
					: (short)(position.y * 2.0));
			case Y_FLOAT -> writer.addFloat((float)position.y);
			case Y_DOUBLE -> writer.addDouble(position.y);
		}

		writeXZ(writer, position.x);
		writeXZ(writer, position.z);

		if ((flags & MASK_ROT) != ROT_0)
		{
			writer.addShort(packRot(rotation[0]));
			writer.addShort(packRot(rotation[1]));
		}

		if ((flags & MASK_ROT) == ROT_HEAD_DIFF) { writer.addShort(packRot(headRot)); }
	}

	private void writeXZ(Writer writer, double val)
	{
		switch (flags & MASK_XZ)
		{
			case XZ_SHORT -> writer.addShort(packValue(val, PACKED_XZ_DIV));
			case XZ_FLOAT -> writer.addFloat((float)val);
			case XZ_DOUBLE -> writer.addDouble(val);
		}
	}

	private boolean isYRelative()
	{
		int yFlags = flags & MASK_Y;
		boolean yShortAbs = (yFlags == Y_SHORT) && ((flags & PACKED_Y) == 0);
		return (yFlags != Y_DOUBLE) && !yShortAbs;
	}

	private boolean isXzRelative()
	{
		int xzFlags = flags & MASK_XZ;
		return xzFlags != XZ_DOUBLE;
	}

	public void applyToPosition(double[] oldPos)
	{
		boolean xzRel = isXzRelative(), yRel = isYRelative();
		oldPos[0] = xzRel ? (oldPos[0] + position.x) : position.x;
		oldPos[1] = yRel ? (oldPos[1] + position.y) : position.y;
		oldPos[2] = xzRel ? (oldPos[2] + position.z) : position.z;
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		Entity entity = ctx.getEntity();
		boolean updateRot = (flags & MASK_ROT) != ROT_0;
		float rotX = updateRot ? rotation[0] : entity.getXRot();
		float rotY = updateRot ? rotation[1] : entity.getYRot();

		float finHeadRot = ctx.getTransformer().transformRotation(headRot);

		ctx.changePosition(position, rotY, rotX, isXzRelative(), isYRelative(), updateRot);
		if (updateRot) { entity.setYHeadRot(finHeadRot); }
		entity.setOnGround((flags & ON_GROUND) != 0);
		((EntityFields)entity).callCheckInsideBlocks();

		ctx.fluentMovement(() -> new ClientboundTeleportEntityPacket(entity)); //TODO: try packet with higher precision
		if (updateRot)
		{
			byte headRotData = (byte)Math.floor(finHeadRot * 256.0f / 360.0f);
			ctx.fluentMovement(() -> new ClientboundRotateHeadPacket(entity, headRotData));
		}
		return Result.OK;
	}

	//TODO: do something with them
	public static class Statistics
	{
		public int count;
		public int y0, yAbsShort, yRelPacked, yRelFloat, yAbsDouble;
		public int xz0, xzRelPacked, xzRelFloat, xzAbsDouble;
		public int rot0, rotHead0, rotHeadEq, rotHeadDiff;
		public int onGroundFalse, onGroundTrue;
		public int errors;

		public void add(Movement movement)
		{
			count++;

			int yInfo = movement.flags & MASK_Y;
			int xzInfo = movement.flags & MASK_XZ;
			int rotInfo = movement.flags & MASK_ROT;
			boolean onGround = (movement.flags & ON_GROUND) != 0;
			boolean packedY = (movement.flags & PACKED_Y) != 0;

			if (!packedY)
			{
				switch (yInfo)
				{
					case Y_0 -> y0++;
					case Y_SHORT -> yAbsShort++;
					case Y_FLOAT -> yRelFloat++;
					case Y_DOUBLE -> yAbsDouble++;
					default -> errors++;
				}
			}
			else
			{
				if (yInfo == Y_SHORT) { yRelPacked++; }
				else { errors++; }
			}

			switch (xzInfo)
			{
				case XZ_0 -> xz0++;
				case XZ_SHORT -> xzRelPacked++;
				case XZ_FLOAT -> xzRelFloat++;
				case XZ_DOUBLE -> xzAbsDouble++;
				default -> errors++;
			}

			switch (rotInfo)
			{
				case ROT_0 -> rot0++;
				case ROT_HEAD_0 -> rotHead0++;
				case ROT_HEAD_EQ -> rotHeadEq++;
				case ROT_HEAD_DIFF -> rotHeadDiff++;
				default -> errors++;
			}

			if (onGround) { onGroundTrue++; }
			else { onGroundFalse++; }
		}
	}
}
