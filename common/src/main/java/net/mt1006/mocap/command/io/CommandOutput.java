package net.mt1006.mocap.command.io;

import net.minecraft.network.chat.Component;

public interface CommandOutput
{
	CommandOutput DUMMY = new DummyCommandOutput();
	CommandOutput LOGS = new LogsCommandOutput();

	boolean sendSuccess(String component, Object... args);
	boolean sendSuccessLiteral(String format, Object... args);
	boolean sendSuccessComponent(Component component);
	boolean sendFailure(String component, Object... args);
	boolean sendFailureWithTip(String component, Object... args);
	boolean sendException(Exception exception, String component, Object... args);
}
