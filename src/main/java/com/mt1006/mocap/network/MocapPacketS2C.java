package com.mt1006.mocap.network;

import com.mt1006.mocap.events.PlayerConnectionEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public class MocapPacketS2C
{
	public static final int ON_LOGIN = 0;
	public static final int NOCOL_PLAYER_ADD = 1;
	public static final int NOCOL_PLAYER_REMOVE = 2;

	private int version;
	private int op;
	private UUID uuid;

	public MocapPacketS2C() { }

	public MocapPacketS2C(FriendlyByteBuf buf)
	{
		version = buf.readInt();
		op = buf.readInt();

		if (op == NOCOL_PLAYER_ADD || op == NOCOL_PLAYER_REMOVE) { uuid = buf.readUUID(); }
	}

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeInt(version);
		buf.writeInt(op);

		if (op == NOCOL_PLAYER_ADD || op == NOCOL_PLAYER_REMOVE) { buf.writeUUID(uuid); }
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (version != MocapPackets.CURRENT_VERSION) { return; }

		switch (op)
		{
			case ON_LOGIN: MocapPacketC2S.send(MocapPacketC2S.ACCEPT_SERVER); break;
			case NOCOL_PLAYER_ADD: PlayerConnectionEvent.addNocolPlayer(uuid); break;
			case NOCOL_PLAYER_REMOVE: PlayerConnectionEvent.removeNocolPlayer(uuid); break;
		}
	}

	public static void send(ServerPlayer serverPlayer, int op, @Nullable UUID uuid)
	{
		MocapPacketS2C packet = new MocapPacketS2C();

		packet.version = MocapPackets.CURRENT_VERSION;
		packet.op = op;

		if (op == NOCOL_PLAYER_ADD || op == NOCOL_PLAYER_REMOVE) { packet.uuid = uuid; }

		MocapPackets.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
	}
}
