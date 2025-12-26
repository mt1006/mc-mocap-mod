package net.mt1006.mocap.mocap.actions;

import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.mixin.fields.EntityFields;
import net.mt1006.mocap.mocap.settings.Settings;
import org.jetbrains.annotations.Nullable;

public class Movement implements MocapAction
{
	/*
	Flags:
	0b------xx:
	  00 - dy=0
	  01 - y as float
	  10 - y as double
	  11 - y*2 as short
	0b----xx--:
	  00 - dx=0, dz=0
	  01 - x and z as float
	  10 - x and z as double
	  11 - not used
	0b--xx----:
	  00 - dxRot=0, dyRot=0, dHeadRot=0
	  01 - xRot and yRot as short, headRot=0
	  10 - xRot and yRot as short, headRot=yRot
	  11 - xRot, yRot and headRot as short
	0b-x------: is on ground
	0bx-------: not used
	 */

	private static final byte Y_DELTA0 =       0b00000000;
	private static final byte Y_FLOAT =        0b00000001;
	private static final byte Y_DOUBLE =       0b00000010;
	private static final byte Y_SHORT =        0b00000011;
	private static final byte XZ_DELTA0 =      0b00000000;
	private static final byte XZ_FLOAT =       0b00000100;
	private static final byte XZ_DOUBLE =      0b00001000;
	private static final byte ROT_DELTA0 =     0b00000000;
	private static final byte ROT_HEAD_0 =     0b00010000;
	private static final byte ROT_HEAD_EQ =    0b00100000;
	private static final byte ROT_HEAD_DIFF =  0b00110000;
	private static final byte ON_GROUND =      0b01000000;

	private static final byte MASK_Y =      0b00000011;
	private static final byte MASK_XZ =     0b00001100;
	private static final byte MASK_ROT =    0b00110000;

	private final byte flags;
	private final Vec3 position;
	private final float[] rotation; // [0]=xRot, [1]=yRot
	private final float headRot;

	//TODO: [CONVERTER] make it private
	public Movement(byte flags, Vec3 position, float[] rotation, float headRot)
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
			case Y_FLOAT -> reader.readFloat();
			case Y_DOUBLE -> reader.readDouble();
			case Y_SHORT -> (double)reader.readShort() / 2.0;
			default -> 0.0; // Y_DELTA0
		};

		x = readXZ(reader);
		z = readXZ(reader);
		position = new Vec3(x, y, z);

		rotation = new float[2];
		if ((flags & MASK_ROT) != ROT_DELTA0)
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
			case XZ_FLOAT -> reader.readFloat();
			case XZ_DOUBLE -> reader.readDouble();
			default -> 0.0; // XZ_DELTA0
		};
	}

	public static @Nullable Movement delta(Vec3 startPos, Vec3 oldPos, Vec3 newPos, float[] oldRot, float newRotX, float newRotY,
										   float oldHeadRot, float newHeadRot, boolean oldOnGround, boolean newOnGround,
										   boolean forceNonPosData)
	{
		byte flags = newOnGround ? ON_GROUND : 0;
		double x = 0.0, y = 0.0, z = 0.0;
		float[] rotation = new float[2];
		float headRot = 0.0f;

		if (oldPos.y != newPos.y)
		{
			double relNewY = newPos.y - startPos.y;
			double newY2 = newPos.y * 2.0;

			if (newY2 == (short)newY2)
			{
				y = newPos.y;
				flags |= Y_SHORT;
			}
			else if (Math.abs(relNewY) > Settings.MAX_FLOAT_POS_VALUE.val)
			{
				y = relNewY;
				flags |= Y_DOUBLE;
			}
			else if ((float)relNewY != (float)(oldPos.y - startPos.y))
			{
				y = relNewY;
				flags |= Y_FLOAT;
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
				flags |= XZ_DOUBLE;
			}
			else if ((float)relNewX != (float)(oldPos.x - startPos.x) || (float)relNewZ != (float)(oldPos.z - startPos.z))
			{
				x = relNewX;
				z = relNewZ;
				flags |= XZ_FLOAT;
			}
		}

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

		return ((flags & ~ON_GROUND) != 0 || oldOnGround != newOnGround || forceNonPosData)
				? new Movement(flags, new Vec3(x, y, z), rotation, headRot)
				: null;
	}

	public static Movement teleportToPos(Vec3 pos, boolean onGround)
	{
		byte flags = (byte)((onGround ? ON_GROUND : 0) | Y_DOUBLE | XZ_DOUBLE | ROT_DELTA0);

		float[] rotArray = new float[2];
		rotArray[0] = 0.0f;
		rotArray[1] = 0.0f;

		return new Movement(flags, pos, rotArray, 0.0f);
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
			case Y_FLOAT -> writer.addFloat((float)position.y);
			case Y_DOUBLE -> writer.addDouble(position.y);
			case Y_SHORT -> writer.addShort((short)(position.y * 2.0));
		}

		writeXZ(writer, position.x);
		writeXZ(writer, position.z);

		if ((flags & MASK_ROT) != ROT_DELTA0)
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
			case XZ_FLOAT -> writer.addFloat((float)val);
			case XZ_DOUBLE -> writer.addDouble(val);
		}
	}

	public Vec3 getNewPosition(Vec3 startPos, Vec3 oldPos)
	{
		boolean xzChanged = (flags & MASK_XZ) != XZ_DELTA0;

		int yFlags = (flags & MASK_Y);
		boolean yRelative = yFlags != Y_DELTA0 && yFlags != Y_SHORT;
		double absoluteY = (yFlags == Y_SHORT) ? position.y : oldPos.y;

		double x = xzChanged ? (startPos.x + position.x) : oldPos.x;
		double y = yRelative ? (startPos.y + position.y) : absoluteY;
		double z = xzChanged ? (startPos.z + position.z) : oldPos.z;
		return new Vec3(x, y, z);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		Entity entity = ctx.getEntity();
		boolean updateRot = (flags & MASK_ROT) != ROT_DELTA0;
		float rotX = updateRot ? rotation[0] : entity.getXRot();
		float rotY = updateRot ? rotation[1] : entity.getYRot();
		float finHeadRot = ctx.getTransformer().transformRotation(headRot);

		Vec3 startPos = ctx.getRecordingData().getStartPos(), oldPos = ctx.getPosition();
		double x = (flags & MASK_XZ) != XZ_DELTA0 ? (position.x + startPos.x) : oldPos.x;
		double z = (flags & MASK_XZ) != XZ_DELTA0 ? (position.z + startPos.z) : oldPos.z;

		double y = switch (flags & MASK_Y)
		{
			case Y_FLOAT, Y_DOUBLE -> position.y + startPos.y;
			case Y_SHORT -> position.y;
			default -> oldPos.y; // Y_DELTA0
		};

		ctx.changePosition(new Vec3(x, y, z), rotY, rotX, updateRot);
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
}
