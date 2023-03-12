package com.mt1006.mocap;

import com.mt1006.mocap.command.RegisterCommand;
import com.mt1006.mocap.events.BlockInteractionEvent;
import com.mt1006.mocap.events.PlayerConnectionEvent;
import com.mt1006.mocap.events.ServerTickEvent;
import com.mt1006.mocap.events.WorldLoadEvent;
import com.mt1006.mocap.network.MocapPackets;
import com.mt1006.mocap.utils.Fields;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MocapMod implements ModInitializer
{
	public static final String MOD_ID = "mocap";
	public static final String VERSION = "1.2.1";
	public static final String FOR_VERSION = "1.19.2";
	public static final String FOR_LOADER = "Fabric";
	public static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onInitialize()
	{
		ServerTickEvents.END_SERVER_TICK.register(ServerTickEvent::onEndTick);
		ServerWorldEvents.UNLOAD.register(WorldLoadEvent::onWorldUnload);
		ServerWorldEvents.UNLOAD.register(WorldLoadEvent::onWorldUnload);
		PlayerBlockBreakEvents.BEFORE.register(BlockInteractionEvent::onBlockBreak);
		UseBlockCallback.EVENT.register(BlockInteractionEvent::onRightClickBlock);
		ServerPlayConnectionEvents.JOIN.register(PlayerConnectionEvent::onPlayerJoin);
		ServerPlayConnectionEvents.DISCONNECT.register(PlayerConnectionEvent::onPlayerLeave);

		RegisterCommand.registerCommands();
		MocapMod.LOGGER.info(getFullName() + " - Author: mt1006 (mt1006x)");
		Fields.init();
		MocapPackets.register();
	}

	public static String getName()
	{
		return "MocapMod v" + VERSION;
	}

	public static String getFullName()
	{
		return "MocapMod v" + VERSION + " for Minecraft " + FOR_VERSION + " [" + FOR_LOADER + "]";
	}
}