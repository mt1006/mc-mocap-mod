package com.mt1006.mocap.utils;

public class Utils
{
	public static byte[] floatToByteArray(float val)
	{
		int bits = Float.floatToIntBits(val);
		return new byte[] { (byte)(bits >> 24), (byte)(bits >> 16), (byte)(bits >> 8), (byte)bits };
	}

	public static float byteArrayToFloat(byte[] bytes)
	{
		int bits = (((int)bytes[0] & 0xFF) << 24) | (((int)bytes[1] & 0xFF) << 16) | (((int)bytes[2] & 0xFF) << 8) | ((int)bytes[3] & 0xFF);
		return Float.intBitsToFloat(bits);
	}

	public static byte[] doubleToByteArray(double val)
	{
		long bits = Double.doubleToLongBits(val);
		return new byte[] { (byte)(bits >> 56), (byte)(bits >> 48), (byte)(bits >> 40), (byte)(bits >> 32),
				(byte)(bits >> 24), (byte)(bits >> 16), (byte)(bits >> 8), (byte)bits };
	}

	public static double byteArrayToDouble(byte[] bytes)
	{
		long bits = (((long)bytes[0] & 0xFF) << 56) | (((long)bytes[1] & 0xFF) << 48) | (((long)bytes[2] & 0xFF) << 40) | (((long)bytes[3] & 0xFF) << 32) |
				(((long)bytes[4] & 0xFF) << 24) | (((long)bytes[5] & 0xFF) << 16) | (((long)bytes[6] & 0xFF) << 8) | ((long)bytes[7] & 0xFF);
		return Double.longBitsToDouble(bits);
	}
}
