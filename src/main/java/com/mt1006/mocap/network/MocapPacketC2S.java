package com.mt1006.mocap.network;

import com.mt1006.mocap.command.InputArgument;
import com.mt1006.mocap.events.PlayerConnectionEvent;
import com.mt1006.mocap.mocap.playing.CustomSkinManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class MocapPacketC2S
{
	public static final int ACCEPT_SERVER = 0;
	public static final int REQUEST_CUSTOM_SKIN = 1;

	private final int version;
	private final int op;
	private final Object object;

	public MocapPacketC2S(int version, int op, Object object)
	{
		this.version = version;
		this.op = op;
		this.object = object;
	}

	public MocapPacketC2S(PacketBuffer buf)
	{
		version = buf.readInt();
		op = buf.readInt();

		switch (op)
		{
			case REQUEST_CUSTOM_SKIN:
				object = NetworkUtils.readString(buf);
				break;

			default:
				object = null;
		}
	}

	public void encode(PacketBuffer buf)
	{
		buf.writeInt(version);
		buf.writeInt(op);

		if (op == REQUEST_CUSTOM_SKIN && object instanceof String)
		{
			NetworkUtils.writeString(buf, (String)object);
		}
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (version != MocapPackets.CURRENT_VERSION) { return; }
		ServerPlayerEntity sender = ctx.get().getSender();

		switch (op)
		{
			case ACCEPT_SERVER:
				PlayerConnectionEvent.addPlayer(sender);
				if (sender != null) { MocapPacketS2C.sendInputSuggestionsAddOnLogin(sender, InputArgument.serverInputSet); }
				break;

			case REQUEST_CUSTOM_SKIN:
				if (object instanceof String) { CustomSkinManager.sendSkinToClient(sender, (String)object); }
				break;
		}
	}

	public static void sendAcceptServer()
	{
		respond(ACCEPT_SERVER, null);
	}

	public static void sendRequestCustomSkin(String name)
	{
		send(REQUEST_CUSTOM_SKIN, name);
	}

	private static void send(int op, Object object)
	{
		MocapPacketC2S packet = new MocapPacketC2S(MocapPackets.CURRENT_VERSION, op, object);
		MocapPackets.INSTANCE.send(PacketDistributor.SERVER.with(() -> null), packet);
	}

	private static void respond(int op, Object object)
	{
		// same as "send", used to prevent bugs when porting to Fabric
		MocapPacketC2S packet = new MocapPacketC2S(MocapPackets.CURRENT_VERSION, op, object);
		MocapPackets.INSTANCE.send(PacketDistributor.SERVER.with(() -> null), packet);
	}
}
