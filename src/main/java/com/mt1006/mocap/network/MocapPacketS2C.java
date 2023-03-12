package com.mt1006.mocap.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class MocapPacketS2C
{
	private final int version;

	public MocapPacketS2C(int version)
	{
		this.version = version;
	}

	public MocapPacketS2C(FriendlyByteBuf buf)
	{
		version = buf.readInt();
	}

	public FriendlyByteBuf encode(FriendlyByteBuf buf)
	{
		buf.writeInt(version);
		return buf;
	}

	public void handle(PacketSender responseSender)
	{
		MocapPacketC2S.send(responseSender, version);
	}

	public static void send(PacketSender sender, int version)
	{
		sender.sendPacket(MocapPackets.CHANNEL_NAME, new MocapPacketS2C(version).encode(PacketByteBufs.create()));
	}

	public static void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender)
	{
		new MocapPacketS2C(buf).handle(responseSender);
	}
}
