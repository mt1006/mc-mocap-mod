package com.mt1006.mocap.events;

import com.mt1006.mocap.network.MocapPacketS2C;
import com.mt1006.mocap.network.MocapPackets;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class PlayerConnectionEvent
{
	private static final int MAX_PLAYER_COUNT = 2048;
	private static final Set<Player> players = Collections.newSetFromMap(new IdentityHashMap<>());

	public static void onPlayerJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server)
	{
		MocapPacketS2C.send(sender, MocapPackets.CURRENT_VERSION);
	}

	public static void onPlayerLeave(ServerGamePacketListenerImpl handler, MinecraftServer server)
	{
		players.remove(handler.getPlayer());
	}

	public static void addPlayer(@Nullable Player player)
	{
		if (player == null || players.size() >= MAX_PLAYER_COUNT) { return; }
		players.add(player);
		players.removeIf(Entity::isRemoved);
	}

	public static boolean isInSet(Player player)
	{
		return players.contains(player);
	}
}
