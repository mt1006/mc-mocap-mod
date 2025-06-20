package com.mt1006.mocap.command;

import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.command.commands.MocapCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.server.command.ConfigCommand;

@EventBusSubscriber(modid = MocapMod.MOD_ID)
public class RegisterCommand
{
	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event)
	{
		MocapCommand.register(event.getDispatcher(), event.getBuildContext());
		ConfigCommand.register(event.getDispatcher());
	}
}
