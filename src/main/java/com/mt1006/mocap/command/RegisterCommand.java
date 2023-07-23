package com.mt1006.mocap.command;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.command.commands.MocapCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

@Mod.EventBusSubscriber(modid = MocapMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RegisterCommand
{
	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event)
	{
		MocapCommand.register(event.getDispatcher());
		ConfigCommand.register(event.getDispatcher());
	}
}
