package com.mt1006.mocap.network;

import com.mt1006.mocap.MocapMod;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MocapMod.MOD_ID)
public class MocapPackets
{
	public static final CustomPacketPayload.Type<CustomPacketPayload> INSTANCE =
			new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MocapMod.MOD_ID, "neoforge"));
	public static final int CURRENT_VERSION = 3;

	@SubscribeEvent
	public static void register(RegisterPayloadHandlersEvent event)
	{
		PayloadRegistrar registrar = event.registrar("1");
		registrar.playToClient(MocapPacketS2C.TYPE, MocapPacketS2C.STREAM_CODEC, MocapPacketS2C::handle);
		registrar.playToServer(MocapPacketC2S.TYPE, MocapPacketC2S.STREAM_CODEC, MocapPacketC2S::handle);
	}
}
