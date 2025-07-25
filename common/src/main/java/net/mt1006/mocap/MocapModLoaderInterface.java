package net.mt1006.mocap;

import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.network.MocapPacketC2S;
import net.mt1006.mocap.network.MocapPacketS2C;

public interface MocapModLoaderInterface
{
	String getLoaderName();
	String getModVersion();
	boolean isModLoaded(String modId);

	void sendPacketToClient(ServerPlayer player, MocapPacketS2C packet);
	void sendPacketToServer(MocapPacketC2S packet);
}
