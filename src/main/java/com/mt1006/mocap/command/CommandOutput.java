package com.mt1006.mocap.command;

import net.minecraft.network.chat.Component;

public class CommandOutput
{
	public static final CommandOutput DUMMY = new CommandOutput();

	public void sendSuccess(String component, Object... args) {}

	public void sendSuccessLiteral(String format, Object... args) {}

	public void sendSuccessComponent(Component component) {}

	public void sendFailure(String component, Object... args) {}

	public void sendException(Exception exception, String component, Object... args) {}
}
