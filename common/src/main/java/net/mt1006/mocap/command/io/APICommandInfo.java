package net.mt1006.mocap.command.io;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.utils.Utils;
import org.jetbrains.annotations.Nullable;

public class APICommandInfo implements CommandInfo
{
	private final MinecraftServer server;
	private final ServerLevel level;
	private final String sourceName;

	public APICommandInfo(MinecraftServer server, ServerLevel level, String sourceName)
	{
		this.server = server;
		this.level = level;
		this.sourceName = "+" + sourceName;
	}

	@Override public boolean sendSuccess(String component, Object... args) { return true; }
	@Override public boolean sendSuccessLiteral(String format, Object... args) { return true; }
	@Override public boolean sendSuccessComponent(Component component) { return true; }

	@Override public boolean sendFailure(String component, Object... args)
	{
		// unlike with LogsCommandOutput failures can be caused by using API, so it's warning, not error
		MocapMod.LOGGER.warn(Utils.stringFromComponent(component, args));
		return false;
	}

	@Override public boolean sendFailureWithTip(String component, Object... args)
	{
		MocapMod.LOGGER.warn(Utils.stringFromComponent(component, args));
		return false;
	}

	@Override public boolean sendException(Exception exception, String component, Object... args)
	{
		Utils.exception(exception, Utils.stringFromComponent(component, args));
		return false;
	}

	@Override public MinecraftServer getServer()
	{
		return server;
	}

	@Override public ServerLevel getLevel()
	{
		return level;
	}

	@Override public @Nullable ServerPlayer getSourcePlayer()
	{
		return null;
	}

	@Override public @Nullable Entity getSourceEntity()
	{
		return null;
	}

	@Override public String getSourceName()
	{
		return sourceName;
	}
}
