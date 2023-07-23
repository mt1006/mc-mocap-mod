package com.mt1006.mocap.network;

import com.mt1006.mocap.command.InputArgument;
import com.mt1006.mocap.events.PlayerConnectionEvent;
import com.mt1006.mocap.mocap.playing.CustomSkinManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

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

	public MocapPacketC2S(FriendlyByteBuf buf)
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

	public FriendlyByteBuf encode(FriendlyByteBuf buf)
	{
		buf.writeInt(version);
		buf.writeInt(op);

		if (op == REQUEST_CUSTOM_SKIN && object instanceof String)
		{
			NetworkUtils.writeString(buf, (String)object);
		}
		return buf;
	}

	public void handle(ServerPlayer player, PacketSender sender)
	{
		if (version != MocapPackets.CURRENT_VERSION) { return; }

		switch (op)
		{
			case ACCEPT_SERVER:
				PlayerConnectionEvent.addPlayer(player);
				if (sender != null) { MocapPacketS2C.sendInputSuggestionsAddOnLogin(sender, InputArgument.serverInputSet); }
				break;

			case REQUEST_CUSTOM_SKIN:
				if (object instanceof String) { CustomSkinManager.sendSkinToClient(player, (String)object); }
				break;
		}
	}

	public static void sendAcceptServer(PacketSender sender)
	{
		respond(sender, ACCEPT_SERVER, null);
	}

	public static void sendRequestCustomSkin(String name)
	{
		send(REQUEST_CUSTOM_SKIN, name);
	}

	private static void send(int op, Object object)
	{
		MocapPacketC2S packet = new MocapPacketC2S(MocapPackets.CURRENT_VERSION, op, object);
		ClientPlayNetworking.send(MocapPackets.CHANNEL_NAME, packet.encode(PacketByteBufs.create()));
	}

	private static void respond(PacketSender sender, int op, Object object)
	{
		MocapPacketC2S packet = new MocapPacketC2S(MocapPackets.CURRENT_VERSION, op, object);
		sender.sendPacket(MocapPackets.CHANNEL_NAME, packet.encode(PacketByteBufs.create()));
	}

	public static void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender sender)
	{
		FriendlyByteBuf bufCopy = new FriendlyByteBuf(buf.copy());
		server.execute(() -> new MocapPacketC2S(bufCopy).handle(player, sender));
	}
}
