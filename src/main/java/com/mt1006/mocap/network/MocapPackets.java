package com.mt1006.mocap.network;

import com.mt1006.mocap.MocapMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

public class MocapPackets
{
	public static final ResourceLocation CHANNEL_NAME = new ResourceLocation(MocapMod.MOD_ID, "fabric");
	public static final int CURRENT_VERSION = 3;

	public static void register()
	{
		ServerPlayNetworking.registerGlobalReceiver(CHANNEL_NAME, MocapPacketC2S::receive);
		if (!MocapMod.isDedicatedServer) { ClientPlayNetworking.registerGlobalReceiver(CHANNEL_NAME, MocapPacketS2C::receive); }
	}
}
