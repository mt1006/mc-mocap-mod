package com.mt1006.mocap.network;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.command.InputArgument;
import com.mt1006.mocap.events.PlayerConnectionEvent;
import com.mt1006.mocap.mocap.playing.CustomSkinManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class MocapPacketC2S implements CustomPacketPayload
{
	public static final int ACCEPT_SERVER = 0;
	public static final int REQUEST_CUSTOM_SKIN = 1;

	private final int version;
	private final int op;
	private final Object object;


	public static final CustomPacketPayload.Type<MocapPacketC2S> TYPE =
			new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MocapMod.MOD_ID, "neoforge_c2s"));

	public static final StreamCodec<ByteBuf, MocapPacketC2S> STREAM_CODEC = StreamCodec.of(
			(buf, packet) -> packet.encode(new FriendlyByteBuf(buf)), (buf) -> new MocapPacketC2S(new FriendlyByteBuf(buf)));

	public static void handle(MocapPacketC2S packet, IPayloadContext ctx)
	{
		Player player = ctx.player();
		if (!(player instanceof ServerPlayer))
		{
			MocapMod.LOGGER.error("C2S payload context contains player that isn't a ServerPlayer!");
			return;
		}

		packet.handle((ServerPlayer)player);
	}

	@Override public @NotNull Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}


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

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeInt(version);
		buf.writeInt(op);

		if (op == REQUEST_CUSTOM_SKIN && object instanceof String)
		{
			NetworkUtils.writeString(buf, (String)object);
		}
	}

	public void handle(ServerPlayer sender)
	{
		if (version != MocapPackets.CURRENT_VERSION) { return; }

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
		ClientPacketDistributor.sendToServer(packet);
	}

	private static void respond(int op, Object object)
	{
		// same as "send", used to prevent bugs when porting to Fabric
		MocapPacketC2S packet = new MocapPacketC2S(MocapPackets.CURRENT_VERSION, op, object);
		ClientPacketDistributor.sendToServer(packet);
	}
}
