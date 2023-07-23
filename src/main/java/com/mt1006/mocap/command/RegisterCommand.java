package com.mt1006.mocap.command;

import com.mt1006.mocap.command.commands.MocapCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class RegisterCommand
{
	public static void registerCommands()
	{
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> MocapCommand.register(dispatcher));
	}
}