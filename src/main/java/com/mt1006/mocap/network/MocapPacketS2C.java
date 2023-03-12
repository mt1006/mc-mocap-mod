package com.mt1006.mocap.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

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

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeInt(version);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		MocapPacketC2S.send(version);
	}

	public static void send(ServerPlayer serverPlayer, int version)
	{
		MocapPackets.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new MocapPacketS2C(version));
	}
}
