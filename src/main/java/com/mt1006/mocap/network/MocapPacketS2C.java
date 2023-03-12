package com.mt1006.mocap.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class MocapPacketS2C
{
	private final int version;

	public MocapPacketS2C(int version)
	{
		this.version = version;
	}

	public MocapPacketS2C(PacketBuffer buf)
	{
		version = buf.readInt();
	}

	public void encode(PacketBuffer buf)
	{
		buf.writeInt(version);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		MocapPacketC2S.send(version);
	}

	public static void send(ServerPlayerEntity serverPlayer, int version)
	{
		MocapPackets.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new MocapPacketS2C(version));
	}
}
