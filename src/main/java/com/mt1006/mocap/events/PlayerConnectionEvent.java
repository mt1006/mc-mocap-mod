package com.mt1006.mocap.events;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.network.MocapPacketS2C;
import com.mt1006.mocap.network.MocapPackets;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Mod.EventBusSubscriber(modid = MocapMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerConnectionEvent
{
	private static final int MAX_PLAYER_COUNT = 2048;
	private static final int MAX_NOCOL_PLAYER_COUNT = 4096;

	public static final Set<Player> players = Collections.newSetFromMap(new IdentityHashMap<>());
	public static final Set<UUID> nocolPlayers = new HashSet<>();

	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent loggedInEvent)
	{
		Player player = loggedInEvent.getEntity();
		if (!(player instanceof ServerPlayer)) { return; }

		MocapPacketS2C.send((ServerPlayer)player, MocapPacketS2C.ON_LOGIN, null);
	}

	@SubscribeEvent
	public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent loggedOutEvent)
	{
		players.remove(loggedOutEvent.getEntity());
	}

	public static void addPlayer(@Nullable Player player)
	{
		if (player == null || players.size() >= MAX_PLAYER_COUNT) { return; }
		players.add(player);
		players.removeIf(Entity::isRemoved);
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
