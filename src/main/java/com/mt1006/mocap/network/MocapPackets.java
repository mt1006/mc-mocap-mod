package com.mt1006.mocap.network;

import com.mt1006.mocap.MocapMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class MocapPackets
{
	public static final int VERSION = 4;

	public static void register()
	{
		PayloadTypeRegistry.playC2S().register(MocapPacketC2S.TYPE, MocapPacketC2S.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(MocapPacketC2S.TYPE, MocapPackets::serverReceiver);

		//TODO: check on dedicated server
		PayloadTypeRegistry.playS2C().register(MocapPacketS2C.TYPE, MocapPacketS2C.CODEC);
		if (!MocapMod.isDedicatedServer)
		{
			ClientPlayNetworking.registerGlobalReceiver(MocapPacketS2C.TYPE, MocapPackets::clientReceiver);
		}
	}

	private static void serverReceiver(MocapPacketC2S packet, ServerPlayNetworking.Context ctx)
	{
		ctx.player().server.execute(() -> packet.handle(ctx.player(), ctx.responseSender()));
	}

	private static void clientReceiver(MocapPacketS2C packet, ClientPlayNetworking.Context ctx)
	{
		ctx.client().execute(() -> packet.handle(ctx.responseSender()));
	}
}
