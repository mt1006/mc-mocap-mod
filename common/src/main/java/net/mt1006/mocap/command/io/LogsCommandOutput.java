package net.mt1006.mocap.command.io;

import net.minecraft.network.chat.Component;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.utils.Utils;

public class LogsCommandOutput implements CommandOutput
{
	@Override public boolean sendSuccess(String component, Object... args) { return true; }
	@Override public boolean sendSuccessLiteral(String format, Object... args) { return true; }
	@Override public boolean sendSuccessComponent(Component component) { return true; }

	@Override public boolean sendFailure(String component, Object... args)
	{
		MocapMod.LOGGER.error(Utils.stringFromComponent(component, args));
		return false;
	}

	@Override public boolean sendFailureWithTip(String component, Object... args)
	{
		MocapMod.LOGGER.error(Utils.stringFromComponent(component, args));
		return false;
	}

	@Override public boolean sendException(Exception exception, String component, Object... args)
	{
		Utils.exception(exception, Utils.stringFromComponent(component, args));
		return false;
	}
}
