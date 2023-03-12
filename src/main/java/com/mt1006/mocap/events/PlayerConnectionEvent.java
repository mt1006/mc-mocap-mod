package com.mt1006.mocap.events;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.network.MocapPacketS2C;
import com.mt1006.mocap.network.MocapPackets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

@Mod.EventBusSubscriber(modid = MocapMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerConnectionEvent
{
	private static final int MAX_PLAYER_COUNT = 2048;
	private static final Set<PlayerEntity> players = Collections.newSetFromMap(new IdentityHashMap<>());

	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent loggedInEvent)
	{
		Entity player = loggedInEvent.getEntity();
		if (!(player instanceof ServerPlayerEntity)) { return; }

		MocapPacketS2C.send((ServerPlayerEntity)player, MocapPackets.CURRENT_VERSION);
	}

	@SubscribeEvent
	public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent loggedOutEvent)
	{
		players.remove(loggedOutEvent.getEntity());
	}

	public static void addPlayer(@Nullable PlayerEntity player)
	{
		if (player == null || players.size() >= MAX_PLAYER_COUNT) { return; }
		players.add(player);
		players.removeIf((p) -> p.removed);
	}

	public static boolean isInSet(PlayerEntity player)
	{
		return players.contains(player);
	}
}
