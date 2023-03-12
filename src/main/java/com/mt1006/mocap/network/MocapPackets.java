package com.mt1006.mocap.network;

import com.mt1006.mocap.IsDedicatedServer;
import com.mt1006.mocap.MocapMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class MocapPackets
{
	public static final ResourceLocation CHANNEL_NAME = new ResourceLocation(MocapMod.MOD_ID, "main");
	public static final int CURRENT_VERSION = 1;

	public static void register()
	{
		ServerPlayNetworking.registerGlobalReceiver(CHANNEL_NAME, MocapPacketC2S::receive);
		if (!IsDedicatedServer.isDedicatedServer) { ClientPlayNetworking.registerGlobalReceiver(CHANNEL_NAME, MocapPacketS2C::receive); }
	}
}
