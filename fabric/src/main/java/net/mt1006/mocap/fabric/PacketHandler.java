package net.mt1006.mocap.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.network.MocapPacketC2S;
import net.mt1006.mocap.network.MocapPacketS2C;

public class PacketHandler
{
	public static void register()
	{
		PayloadTypeRegistry.playC2S().register(MocapPacketC2S.TYPE, MocapPacketC2S.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(MocapPacketC2S.TYPE, PacketHandler::serverReceiver);

		PayloadTypeRegistry.playS2C().register(MocapPacketS2C.TYPE, MocapPacketS2C.CODEC);
		if (!MocapMod.isDedicatedServer)
		{
			ClientPlayNetworking.registerGlobalReceiver(MocapPacketS2C.TYPE, PacketHandler::clientReceiver);
		}
	}

	private static void serverReceiver(MocapPacketC2S packet, ServerPlayNetworking.Context ctx)
	{
		ctx.player().server.execute(() -> packet.handle(new Client(ctx)));
	}

	private static void clientReceiver(MocapPacketS2C packet, ClientPlayNetworking.Context ctx)
	{
		ctx.client().execute(() -> packet.handle(new Server(ctx)));
	}

	public static class Client implements MocapPacketC2S.Client
	{
		private final ServerPlayer player;
		private final PacketSender sender;

		public Client(ServerPlayer player, PacketSender sender)
		{
			this.player = player;
			this.sender = sender;
		}

		public Client(ServerPlayNetworking.Context ctx)
		{
			player = ctx.player();
			sender = ctx.responseSender();
		}

		@Override public ServerPlayer getPlayer()
		{
			return player;
		}

		@Override public void respond(MocapPacketS2C packet)
		{
			sender.sendPacket(packet);
		}
	}

	public static class Server implements MocapPacketS2C.Server
	{
		private final PacketSender sender;

		public Server(ClientPlayNetworking.Context ctx)
		{
			sender = ctx.responseSender();
		}

		@Override public void respond(MocapPacketC2S packet)
		{
			sender.sendPacket(packet);
		}
	}
}
