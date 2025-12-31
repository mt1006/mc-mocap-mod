package net.mt1006.mocap.network;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.events.PlayerConnectionEvent;
import net.mt1006.mocap.mocap.playing.skins.CustomClientSkinManager;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.UUID;

public class MocapPacketS2C implements CustomPacketPayload
{
	private static final String TYPE_ID = MocapMod.loaderInterface.getLoaderName().toLowerCase(Locale.ROOT) + "_s2c";
	public static final Type<MocapPacketS2C> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MocapMod.MOD_ID, TYPE_ID));
	public static final StreamCodec<FriendlyByteBuf, MocapPacketS2C> CODEC = StreamCodec.of((b, p) -> p.encode(b), MocapPacketS2C::new);

	public static final int ON_LOGIN = 0;
	public static final int NOCOL_PLAYER_ADD = 1;
	public static final int NOCOL_PLAYER_REMOVE = 2;
	public static final int CUSTOM_SKIN_DATA = 5;
	private static final int UNUSED = 6; // CLEAR_CACHE in some 1.4 alpha versions

	private final int version;
	private final int op;
	private final Object object;

	public MocapPacketS2C(int op, Object object)
	{
		this.version = MocapMod.NETWORK_PACKETS_VERSION;
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

			case CUSTOM_SKIN_DATA:
				String customSkinName = NetworkUtils.readString(buf);
				byte[] customSkinArray = NetworkUtils.readByteArray(buf);
				object = Pair.of(customSkinName, customSkinArray);
				break;

			default:
				object = null;
		}
	}

	@Override public @NotNull Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeInt(version);
		buf.writeInt(op);

		switch (op)
		{
			case NOCOL_PLAYER_ADD:
			case NOCOL_PLAYER_REMOVE:
				if (object instanceof UUID) { buf.writeUUID((UUID)object); }
				break;

			case CUSTOM_SKIN_DATA:
				if (!(object instanceof Pair<?,?>)) { break; }
				Pair<String, byte[]> customSkinData = (Pair<String, byte[]>)object;
				NetworkUtils.writeString(buf, customSkinData.getFirst());
				NetworkUtils.writeByteArray(buf, customSkinData.getSecond());
				break;
		}
	}

	public void handle(Server server)
	{
		if (version != MocapMod.NETWORK_PACKETS_VERSION) { return; }

 		switch (op)
		{
			case ON_LOGIN: MocapPacketC2S.sendAcceptServer(server); break;
			case NOCOL_PLAYER_ADD: PlayerConnectionEvent.addNocolPlayer((UUID)object); break;
			case NOCOL_PLAYER_REMOVE: PlayerConnectionEvent.removeNocolPlayer((UUID)object); break;
			case CUSTOM_SKIN_DATA: CustomClientSkinManager.register((Pair<String, byte[]>)object); break;
		}
	}

	public static void sendOnLogin(MocapPacketC2S.Client client)
	{
		client.respond(new MocapPacketS2C(ON_LOGIN, null));
	}

	public static void sendNocolPlayerAdd(ServerPlayer serverPlayer, UUID playerToAdd)
	{
		send(serverPlayer, NOCOL_PLAYER_ADD, playerToAdd);
	}

	public static void sendNocolPlayerRemove(ServerPlayer serverPlayer, UUID playerToAdd)
	{
		send(serverPlayer, NOCOL_PLAYER_REMOVE, playerToAdd);
	}

	public static void sendCustomSkinData(ServerPlayer player, String name, byte[] byteArray)
	{
		send(player, CUSTOM_SKIN_DATA, Pair.of(name, byteArray));
	}

	private static void send(ServerPlayer player, int op, Object object)
	{
		MocapMod.loaderInterface.sendPacketToClient(player, new MocapPacketS2C(op, object));
	}

	public interface Server
	{
		void respond(MocapPacketC2S packet);
	}
}
