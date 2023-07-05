package com.mt1006.mocap.network;

import net.minecraft.network.PacketBuffer;

import java.nio.charset.StandardCharsets;

public class NetworkUtils
{
	public static void writeString(PacketBuffer buf, String str)
	{
		buf.writeInt(str.length());
		buf.writeBytes(str.getBytes(StandardCharsets.UTF_8));
	}

	public static String readString(PacketBuffer buf)
	{
		int length = buf.readInt();
		byte[] byteArray = new byte[length];
		for (int i = 0; i < length; i++)
		{
			byteArray[i] = buf.readByte();
		}
		return new String(byteArray, 0, length, StandardCharsets.UTF_8);
	}

	public static void writeByteArray(PacketBuffer buf, byte[] array)
	{
		buf.writeInt(array.length);
		buf.writeBytes(array);
	}

	public static byte[] readByteArray(PacketBuffer buf)
	{
		int length = buf.readInt();
		byte[] byteArray = new byte[length];
		for (int i = 0; i < length; i++)
		{
			byteArray[i] = buf.readByte();
		}
		return byteArray;
	}
}
