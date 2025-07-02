package net.mt1006.mocap.command.io;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.mt1006.mocap.utils.Utils;
import org.jetbrains.annotations.Nullable;

public interface CommandInfo extends CommandOutput
{
	MinecraftServer getServer();
	ServerLevel getLevel();
	@Nullable ServerPlayer getSourcePlayer();
	@Nullable Entity getSourceEntity();
	String getSourceName();

	default MutableComponent getTranslatableComponent(String component, Object... args)
	{
		return Utils.getTranslatableComponent(getSourceEntity(), component, args);
	}
}
