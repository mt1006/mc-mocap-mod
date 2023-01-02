package com.mt1006.mocap.events;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.mocap.commands.Settings;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MocapMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldLoadEvent
{
	@SubscribeEvent
	public static void onWorldUnload(LevelEvent.Unload unloadEvent)
	{
		LevelAccessor world = unloadEvent.getLevel();

		if (!world.isClientSide()) { Settings.unload(); }
	}
}
