package net.mt1006.mocap.command.io;

import net.minecraft.network.chat.Component;

class DummyCommandOutput implements CommandOutput
{
	@Override public boolean sendSuccess(String component, Object... args) { return true; }
	@Override public boolean sendSuccessLiteral(String format, Object... args) { return true; }
	@Override public boolean sendSuccessComponent(Component component) { return true; }
	@Override public boolean sendFailure(String component, Object... args) { return false; }
	@Override public boolean sendFailureWithTip(String component, Object... args) { return false; }
	@Override public boolean sendException(Exception exception, String component, Object... args) { return false; }
}
