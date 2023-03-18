package com.mt1006.mocap.network;

import com.mt1006.mocap.MocapMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class MocapPackets
{
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(MocapMod.MOD_ID, "main"), () -> PROTOCOL_VERSION, (str) -> true, (str) -> true);
	public static final int CURRENT_VERSION = 2;

	public static void register()
	{
		INSTANCE.messageBuilder(MocapPacketS2C.class, 0, NetworkDirection.PLAY_TO_CLIENT)
				.decoder(MocapPacketS2C::new)
				.encoder(MocapPacketS2C::encode)
				.consumerMainThread(MocapPacketS2C::handle)
				.add();

		INSTANCE.messageBuilder(MocapPacketC2S.class, 1, NetworkDirection.PLAY_TO_SERVER)
				.decoder(MocapPacketC2S::new)
				.encoder(MocapPacketC2S::encode)
				.consumerMainThread(MocapPacketC2S::handle)
				.add();
	}

}
