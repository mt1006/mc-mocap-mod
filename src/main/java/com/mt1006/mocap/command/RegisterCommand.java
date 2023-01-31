package com.mt1006.mocap.command;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class RegisterCommand
{
	public static void registerCommands()
	{
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> MocapCommand.register(dispatcher));
	}
}