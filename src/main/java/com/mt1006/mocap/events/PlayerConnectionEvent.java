package com.mt1006.mocap.events;

import com.mt1006.mocap.network.MocapPacketS2C;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerConnectionEvent
{
	private static final int MAX_PLAYER_COUNT = 2048;
	private static final int MAX_NOCOL_PLAYER_COUNT = 4096;

	public static final Set<ServerPlayer> players = Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
	public static final Set<UUID> nocolPlayers = Collections.synchronizedSet(new HashSet<>());

	public static void onPlayerJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server)
	{

		MocapPacketS2C.sendOnLogin(sender);
	}

	public static void onPlayerLeave(ServerGamePacketListenerImpl handler, MinecraftServer server)
	{
		players.remove(handler.player);
	}

	public static void addPlayer(@Nullable ServerPlayer player)
	{
		if (player == null || players.size() >= MAX_PLAYER_COUNT) { return; }
		players.add(player);
		players.removeIf((e) -> e.removed);
	}

	public static void addNocolPlayer(UUID uuid)
	{
		if (nocolPlayers.size() >= MAX_NOCOL_PLAYER_COUNT) { return; }
		nocolPlayers.add(uuid);
	}

	public static void removeNocolPlayer(UUID uuid)
	{
		nocolPlayers.remove(uuid);
	}
}
