package com.mt1006.mocap.network;

import com.mt1006.mocap.events.PlayerConnectionEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class MocapPacketC2S
{
	public static final int ACCEPT_SERVER = 0;

	private int version;
	private int op;

	public MocapPacketC2S() { }

	public MocapPacketC2S(FriendlyByteBuf buf)
	{
		version = buf.readInt();
		op = buf.readInt();
	}

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeInt(version);
		buf.writeInt(op);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (version != MocapPackets.CURRENT_VERSION) { return; }

		if (op == ACCEPT_SERVER) { PlayerConnectionEvent.addPlayer(ctx.get().getSender()); }
	}

	public static void send(int op)
	{
		MocapPacketC2S packet = new MocapPacketC2S();

		packet.version = MocapPackets.CURRENT_VERSION;
		packet.op = op;

		MocapPackets.INSTANCE.send(PacketDistributor.SERVER.with(() -> null), packet);
	}
}
