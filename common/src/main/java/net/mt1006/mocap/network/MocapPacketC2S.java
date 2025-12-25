package net.mt1006.mocap.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.events.PlayerConnectionEvent;
import net.mt1006.mocap.mocap.playing.skins.CustomServerSkinManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class MocapPacketC2S implements CustomPacketPayload
{
	private static final String TYPE_ID = MocapMod.loaderInterface.getLoaderName().toLowerCase(Locale.ROOT) + "_c2s";
	public static final Type<MocapPacketC2S> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MocapMod.MOD_ID, TYPE_ID));
	public static final StreamCodec<FriendlyByteBuf, MocapPacketC2S> CODEC = StreamCodec.of((b, p) -> p.encode(b), MocapPacketC2S::new);

	public static final int ACCEPT_SERVER = 0;
	public static final int REQUEST_CUSTOM_SKIN = 1;

	private final int version;
	private final int op;
	private final Object object;

	public MocapPacketC2S(int op, Object object)
	{
		this.version = MocapMod.NETWORK_PACKETS_VERSION;
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

	@Override public @NotNull Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
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

	public void handle(Client client)
	{
		if (version != MocapMod.NETWORK_PACKETS_VERSION) { return; }
		ServerPlayer player = client.getPlayer();

		switch (op)
		{
			case ACCEPT_SERVER:
				PlayerConnectionEvent.addPlayer(player);
				PlayerConnectionEvent.experimentalReleaseWarning(player);
				break;

			case REQUEST_CUSTOM_SKIN:
				if (object instanceof String) { CustomServerSkinManager.sendSkinToClient(player, (String)object); }
				break;
		}
	}

	public static void sendAcceptServer(MocapPacketS2C.Server server)
	{
		server.respond(new MocapPacketC2S(ACCEPT_SERVER, null));
	}

	public static void sendRequestCustomSkin(String name)
	{
		send(REQUEST_CUSTOM_SKIN, name);
	}

	private static void send(int op, Object object)
	{
		MocapMod.loaderInterface.sendPacketToServer(new MocapPacketC2S(op, object));
	}

	public interface Client
	{
		@Nullable ServerPlayer getPlayer();
		void respond(MocapPacketS2C packet);
	}
}
