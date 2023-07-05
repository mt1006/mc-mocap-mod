package com.mt1006.mocap.network;

import com.mojang.datafixers.util.Pair;
import com.mt1006.mocap.command.InputArgument;
import com.mt1006.mocap.events.PlayerConnectionEvent;
import com.mt1006.mocap.mocap.playing.CustomClientSkinManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class MocapPacketS2C
{
	public static final int ON_LOGIN = 0;
	public static final int NOCOL_PLAYER_ADD = 1;
	public static final int NOCOL_PLAYER_REMOVE = 2;
	public static final int INPUT_SUGGESTIONS_ADD = 3;
	public static final int INPUT_SUGGESTIONS_REMOVE = 4;
	public static final int CUSTOM_SKIN_DATA = 5;

	private final int version;
	private final int op;
	private final Object object;

	public MocapPacketS2C(int version, int op, Object object)
	{
		this.version = version;
		this.op = op;
		this.object = object;
	}

	public MocapPacketS2C(FriendlyByteBuf buf)
	{
		version = buf.readInt();
		op = buf.readInt();

		switch (op)
		{
			case NOCOL_PLAYER_ADD:
			case NOCOL_PLAYER_REMOVE:
				object = buf.readUUID();
				break;

			case INPUT_SUGGESTIONS_ADD:
			case INPUT_SUGGESTIONS_REMOVE:
				int suggestionListSize = buf.readInt();
				Collection<String> suggestionList = new ArrayList<>(suggestionListSize);
				for (int i = 0; i < suggestionListSize; i++)
				{
					suggestionList.add(NetworkUtils.readString(buf));
				}
				object = suggestionList;
				break;

			case CUSTOM_SKIN_DATA:
				String customSkinName = NetworkUtils.readString(buf);
				byte[] customSkinArray = NetworkUtils.readByteArray(buf);
				object = new Pair<>(customSkinName, customSkinArray);
				break;

			default:
				object = null;
		}
	}

	public FriendlyByteBuf encode(FriendlyByteBuf buf)
	{
		buf.writeInt(version);
		buf.writeInt(op);

		switch (op)
		{
			case NOCOL_PLAYER_ADD:
			case NOCOL_PLAYER_REMOVE:
				if (object instanceof UUID) { buf.writeUUID((UUID)object); }
				break;

			case INPUT_SUGGESTIONS_ADD:
			case INPUT_SUGGESTIONS_REMOVE:
				if (!(object instanceof Collection<?>)) { break; }
				buf.writeInt(((Collection<?>)object).size());
				for (Object str : (Collection<?>)object)
				{
					if (str instanceof String) { NetworkUtils.writeString(buf, (String)str); }
				}
				break;

			case CUSTOM_SKIN_DATA:
				if (!(object instanceof Pair<?,?>)) { break; }
				Pair<String, byte[]> customSkinData = (Pair<String, byte[]>)object;
				NetworkUtils.writeString(buf, customSkinData.getFirst());
				NetworkUtils.writeByteArray(buf, customSkinData.getSecond());
				break;
		}
		return buf;
	}

	public void handle(PacketSender sender)
	{
		if (version != MocapPackets.CURRENT_VERSION) { return; }

		switch (op)
		{
			case ON_LOGIN: MocapPacketC2S.sendAcceptServer(sender); break;
			case NOCOL_PLAYER_ADD: PlayerConnectionEvent.addNocolPlayer((UUID)object); break;
			case NOCOL_PLAYER_REMOVE: PlayerConnectionEvent.removeNocolPlayer((UUID)object); break;
			case INPUT_SUGGESTIONS_ADD: InputArgument.clientInputSet.addAll((Collection<String>)object); break;
			case INPUT_SUGGESTIONS_REMOVE: InputArgument.clientInputSet.removeAll((Collection<String>)object); break;
			case CUSTOM_SKIN_DATA: CustomClientSkinManager.register((Pair<String, byte[]>)object); break;
		}
	}

	public static void sendOnLogin(PacketSender sender)
	{
		sendToSender(sender, ON_LOGIN, null);
	}

	public static void sendNocolPlayerAdd(ServerPlayer serverPlayer, UUID player)
	{
		send(serverPlayer, NOCOL_PLAYER_ADD, player);
	}

	public static void sendNocolPlayerRemove(ServerPlayer serverPlayer, UUID player)
	{
		send(serverPlayer, NOCOL_PLAYER_REMOVE, player);
	}

	public static void sendInputSuggestionsAdd(ServerPlayer serverPlayer, Collection<String> strings)
	{
		send(serverPlayer, INPUT_SUGGESTIONS_ADD, strings);
	}

	public static void sendInputSuggestionsAdd(PacketSender sender, Collection<String> strings)
	{
		sendToSender(sender, INPUT_SUGGESTIONS_ADD, strings);
	}

	public static void sendInputSuggestionsRemove(ServerPlayer serverPlayer, Collection<String> strings)
	{
		send(serverPlayer, INPUT_SUGGESTIONS_REMOVE, strings);
	}

	public static void sendCustomSkinData(ServerPlayer serverPlayer, String name, byte[] byteArray)
	{
		send(serverPlayer, CUSTOM_SKIN_DATA, new Pair<>(name, byteArray));
	}

	private static void send(ServerPlayer player, int op, Object object)
	{
		MocapPacketS2C packet = new MocapPacketS2C(MocapPackets.CURRENT_VERSION, op, object);
		ServerPlayNetworking.send(player, MocapPackets.CHANNEL_NAME, packet.encode(PacketByteBufs.create()));
	}

	private static void sendToSender(PacketSender sender, int op, Object object)
	{
		MocapPacketS2C packet = new MocapPacketS2C(MocapPackets.CURRENT_VERSION, op, object);
		sender.sendPacket(MocapPackets.CHANNEL_NAME, packet.encode(PacketByteBufs.create()));
	}

	public static void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender sender)
	{
		new MocapPacketS2C(buf).handle(sender);
	}
}
