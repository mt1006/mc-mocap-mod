package com.mt1006.mocap;

import com.mt1006.mocap.utils.Fields;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MocapMod.MOD_ID)
@Mod.EventBusSubscriber(modid = MocapMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MocapMod
{
	public static final String MOD_ID = "mocap";
	public static final String VERSION = "1.2";
	public static final String FOR_VERSION = "1.19.3";
	public static final String FOR_LOADER = "Forge";
	public static final Logger LOGGER = LogManager.getLogger();

	public MocapMod()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public static void setup(final FMLCommonSetupEvent event)
	{
		MocapMod.LOGGER.info(getFullName() + " - Author: mt1006 (mt1006x)");
		Fields.init();
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
