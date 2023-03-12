package com.mt1006.mocap.network;

import com.mt1006.mocap.events.PlayerConnectionEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

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

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeInt(version);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		PlayerConnectionEvent.addPlayer(ctx.get().getSender());
	}

	public static void send(int version)
	{
		MocapPackets.INSTANCE.send(PacketDistributor.SERVER.with(() -> null), new MocapPacketC2S(version));
	}
}
