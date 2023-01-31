package com.mt1006.mocap.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class RegisterCommand
{
	public static void registerCommands()
	{
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> MocapCommand.register(dispatcher));
	}
}