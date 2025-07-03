package net.mt1006.mocap.neoforge;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.network.MocapPacketC2S;
import net.mt1006.mocap.network.MocapPacketS2C;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = MocapMod.MOD_ID)
public class PacketHandler
{
	@SubscribeEvent
	public static void register(RegisterPayloadHandlersEvent event)
	{
		PayloadRegistrar registrar = event.registrar("1");
		registrar.playToServer(MocapPacketC2S.TYPE, MocapPacketC2S.CODEC, PacketHandler::serverReceiver);
		registrar.playToClient(MocapPacketS2C.TYPE, MocapPacketS2C.CODEC, PacketHandler::clientReceiver);
	}

	private static void serverReceiver(MocapPacketC2S packet, IPayloadContext ctx)
	{
		packet.handle(new Client(ctx));
	}

	private static void clientReceiver(MocapPacketS2C packet, IPayloadContext ctx)
	{
		packet.handle(new Server());
	}

	public static class Client implements MocapPacketC2S.Client
	{
		private final @Nullable ServerPlayer player;

		public Client(@Nullable ServerPlayer player)
		{
			this.player = player;
		}

		public Client(IPayloadContext ctx)
		{
			Player player = ctx.player();
			this.player = (player instanceof ServerPlayer) ? (ServerPlayer)player : null;
		}

		@Override public @Nullable ServerPlayer getPlayer()
		{
			return player;
		}

		@Override public void respond(MocapPacketS2C packet)
		{
			if (player == null) { return; }
			PacketDistributor.sendToPlayer(player, packet);
		}
	}

	public static class Server implements MocapPacketS2C.Server
	{
		@Override public void respond(MocapPacketC2S packet)
		{
			ClientPacketDistributor.sendToServer(packet);
		}
	}
}
