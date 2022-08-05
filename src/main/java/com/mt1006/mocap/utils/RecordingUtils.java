package com.mt1006.mocap.utils;

import net.minecraft.world.entity.Pose;

import java.util.ArrayList;
import java.util.Arrays;

public class RecordingUtils
{
	public static class RecordingReader
	{
		public byte[] recording;
		public int offset = 0;

		public byte readByte()
		{
			byte retVal = recording[offset];
			offset++;
			return retVal;
		}

		public int readInt()
		{
			int retVal = (int)((recording[offset] & 0xFF) << 24) |
					(int)((recording[offset + 1] & 0xFF) << 16) |
					(int)((recording[offset + 2] & 0xFF) << 8) |
					(int)(recording[offset + 3] & 0xFF);
			offset += 4;
			return retVal;
		}
		public float readFloat()
		{
			float retVal = Utils.byteArrayToFloat(Arrays.copyOfRange(recording, offset, offset + 4));
			offset += 4;
			return retVal;
		}

		public double readDouble()
		{
			double retVal = Utils.byteArrayToDouble(Arrays.copyOfRange(recording, offset, offset + 8));
			offset += 8;
			return retVal;
		}

		public boolean readBoolean()
		{
			boolean retVal = (recording[offset] == 1);
			offset++;
			return retVal;
		}

		public Pose readPose()
		{
			int val = readInt();
			if (val == 1) { return Pose.STANDING; }
			if (val == 2) { return Pose.FALL_FLYING; }
			if (val == 3) { return Pose.SLEEPING; }
			if (val == 4) { return Pose.SWIMMING; }
			if (val == 5) { return Pose.SPIN_ATTACK; }
			if (val == 6) { return Pose.CROUCHING; }
			if (val == 7) { return Pose.DYING; }
			return Pose.STANDING;
		}
	}

	public static void addByte(ArrayList<Byte> recording, byte val)
	{
		recording.add(val);
	}

	public static void addInt(ArrayList<Byte> recording, int val)
	{
		recording.add((byte)(val >> 24));
		recording.add((byte)(val >> 16));
		recording.add((byte)(val >> 8));
		recording.add((byte)val);
	}

	public static void addFloat(ArrayList<Byte> recording, float val)
	{
		for (byte b : Utils.floatToByteArray(val))
		{
			recording.add(b);
		}
	}

	public static void addDouble(ArrayList<Byte> recording, double val)
	{
		for (byte b : Utils.doubleToByteArray(val))
		{
			recording.add(b);
		}
	}

	public static void addBoolean(ArrayList<Byte> recording, boolean val)
	{
		if (val) { recording.add((byte)1); }
		else { recording.add((byte)0); }
	}

	public static void addPose(ArrayList<Byte> recording, Pose pose)
	{
		if (pose == Pose.STANDING) { addInt(recording, 1); }
		else if (pose == Pose.FALL_FLYING) { addInt(recording, 2); }
		else if (pose == Pose.SLEEPING) { addInt(recording, 3); }
		else if (pose == Pose.SWIMMING) { addInt(recording, 4); }
		else if (pose == Pose.SPIN_ATTACK) { addInt(recording, 5); }
		else if (pose == Pose.CROUCHING) { addInt(recording, 6); }
		else if (pose == Pose.DYING) { addInt(recording, 7); }
		else { addInt(recording, 0); }
	}
}
