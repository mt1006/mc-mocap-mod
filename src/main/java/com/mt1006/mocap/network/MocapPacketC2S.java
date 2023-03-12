package com.mt1006.mocap.network;

import com.mt1006.mocap.events.PlayerConnectionEvent;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class MocapPacketC2S
{
	private final int version;

	public MocapPacketC2S(int version)
	{
		this.version = version;
	}

	public MocapPacketC2S(FriendlyByteBuf buf)
	{
		version = buf.readInt();
	}

	public FriendlyByteBuf encode(FriendlyByteBuf buf)
	{
		buf.writeInt(version);
		return buf;
	}

	public void handle(ServerPlayer player)
	{
		PlayerConnectionEvent.addPlayer(player);
	}

	public static void send(PacketSender sender, int version)
	{
		sender.sendPacket(MocapPackets.CHANNEL_NAME, new MocapPacketC2S(version).encode(PacketByteBufs.create()));
	}

	public static void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender sender)
	{
		new MocapPacketC2S(buf).handle(player);
	}
}
