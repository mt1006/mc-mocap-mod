package com.mt1006.mocap;

import com.mt1006.mocap.mocap.actions.Action;
import com.mt1006.mocap.utils.Fields;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MocapMod.MOD_ID)
@EventBusSubscriber(modid = MocapMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class MocapMod
{
	public static final String MOD_ID = "mocap";
	public static final String VERSION = "1.3.9";
	public static final String FOR_VERSION = "1.21.3";
	public static final String FOR_LOADER = "NeoForge";
	public static final Logger LOGGER = LogManager.getLogger();
	public static final boolean isDedicatedServer = FMLEnvironment.dist.isDedicatedServer();

	@SubscribeEvent
	public static void setup(final FMLCommonSetupEvent event)
	{
		Fields.init();
		Action.init();
	}

	public static String getName()
	{
		return "Mocap v" + VERSION;
	}

	public static String getFullName()
	{
		return "Mocap v" + VERSION + " for Minecraft " + FOR_VERSION + " [" + FOR_LOADER + "]";
	}
}
