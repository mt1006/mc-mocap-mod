package net.mt1006.mocap.command.converter;

import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.mocap.actions.Movement;
import net.mt1006.mocap.mocap.settings.Settings;

//TODO: [CONVERTER] remove
public class AlphaMovement
{
	private static final byte Y_SHORT =        0b00000001;
	private static final byte Y_FLOAT =        0b00000010;
	private static final byte Y_DOUBLE =       0b00000011;
	private static final byte XZ_SHORT =       0b00000100;
	private static final byte XZ_FLOAT =       0b00001000;
	private static final byte XZ_DOUBLE =      0b00001100;
	private static final byte ROT_0 =          0b00000000;
	private static final byte ROT_HEAD_EQ =    0b00100000;
	private static final byte ROT_HEAD_DIFF =  0b00110000;
	private static final byte ON_GROUND =      0b01000000;
	private static final byte PACKED_Y = (byte)0b10000000;

	private static final byte MASK_Y =      0b00000011;
	private static final byte MASK_XZ =     0b00001100;
	private static final byte MASK_ROT =    0b00110000;

	private static final double PACKED_Y_DIV = 4.0;
	private static final double PACKED_XZ_DIV = 2.0;


	private final byte flags;
	private final Vec3 position;
	private final float[] rotation; // [0]=xRot, [1]=yRot
	private final float headRot;

	public AlphaMovement(MocapAction.Reader reader)
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

	private double readXZ(MocapAction.Reader reader)
	{
		return switch (flags & MASK_XZ)
		{
			case XZ_SHORT -> unpackValue(reader.readShort(), PACKED_XZ_DIV);
			case XZ_FLOAT -> reader.readFloat();
			case XZ_DOUBLE -> reader.readDouble();
			default -> 0.0; // XZ_0
		};
	}

	private static double unpackValue(short packed, double div)
	{
		return ((double)packed / (double)Short.MAX_VALUE) * div;
	}

	private static float unpackRot(short packed)
	{
		return (float)(((double)packed / (double)0x10000) * 360.0);
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

	public void applyToPosition(double[] currentPos)
	{
		boolean xzRel = isXzRelative(), yRel = isYRelative();
		currentPos[0] = xzRel ? (currentPos[0] + position.x) : position.x;
		currentPos[1] = yRel ? (currentPos[1] + position.y) : position.y;
		currentPos[2] = xzRel ? (currentPos[2] + position.z) : position.z;
	}

	public Movement convert(Vec3 startPos, double[] currentPos)
	{
		byte newFlags = 0;
		double x = 0.0, y = 0.0, z = 0.0;

		Vec3 oldPos = new Vec3(currentPos[0], currentPos[1], currentPos[2]);
		applyToPosition(currentPos);
		Vec3 newPos = new Vec3(currentPos[0], currentPos[1], currentPos[2]);

		if (oldPos.y != newPos.y)
		{
			double relNewY = newPos.y - startPos.y;
			double newY2 = newPos.y * 2.0;

			if (newY2 == (short)newY2)
			{
				y = newPos.y;
				newFlags |= 0b00000011;
			}
			else if (Math.abs(relNewY) > Settings.MAX_FLOAT_POS_VALUE.val)
			{
				y = relNewY;
				newFlags |= 0b00000010;
			}
			else if ((float)relNewY != (float)(oldPos.y - startPos.y))
			{
				y = relNewY;
				newFlags |= 0b00000001;
			}
		}

		if (oldPos.x != newPos.x || oldPos.z != newPos.z)
		{
			double relNewX = newPos.x - startPos.x;
			double relNewZ = newPos.z - startPos.z;

			if (Math.abs(relNewX) > Settings.MAX_FLOAT_POS_VALUE.val || Math.abs(relNewZ) > Settings.MAX_FLOAT_POS_VALUE.val)
			{
				x = relNewX;
				z = relNewZ;
				newFlags |= 0b00001000;
			}
			else if ((float)relNewX != (float)(oldPos.x - startPos.x) || (float)relNewZ != (float)(oldPos.z - startPos.z))
			{
				x = relNewX;
				z = relNewZ;
				newFlags |= 0b00000100;
			}
		}

		newFlags |= (byte)(flags & MASK_ROT);
		newFlags |= (byte)(flags & ON_GROUND);
		return new Movement(newFlags, new Vec3(x, y, z), rotation, headRot);
	}
}
