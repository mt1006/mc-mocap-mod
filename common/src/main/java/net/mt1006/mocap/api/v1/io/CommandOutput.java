package net.mt1006.mocap.api.v1.io;

import net.minecraft.network.chat.Component;
import net.mt1006.mocap.command.io.DummyCommandOutput;
import net.mt1006.mocap.command.io.LogsCommandOutput;

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
