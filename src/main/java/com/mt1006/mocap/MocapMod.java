package com.mt1006.mocap;

import com.mt1006.mocap.command.RegisterCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MocapMod implements ModInitializer
{
	public static final String MOD_ID = "mocap";
	public static final String VERSION = "1.0";
	public static final String FOR_VERSION = "1.18.2";
	public static final String FOR_LOADER = "Fabric";
	public static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onInitialize()
	{
		ServerTickEvents.END_SERVER_TICK.register((server -> ServerTickEvent.onEndTick(server)));
		RegisterCommands.registerCommands();
		MocapMod.LOGGER.info(getFullName() + " - Author: mt1006 (mt1006x)");
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
