package com.mt1006.mocap.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mt1006.mocap.mocap.playing.PlayerData;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class CommandInfo extends CommandOutput
{
	public final CommandContext<CommandSource> ctx;
	public final CommandSource source;

	public CommandInfo(CommandContext<CommandSource> ctx)
	{
		this.ctx = ctx;
		this.source = ctx.getSource();
	}

	@Override public void sendSuccess(String component, Object... args)
	{
		source.sendSuccess(Utils.getTranslatableComponent(source.getEntity(), component, args), false);
	}

	@Override public void sendSuccessLiteral(String format, Object... args)
	{
		source.sendSuccess(new StringTextComponent(String.format(format, args)), false);
	}

	@Override public void sendSuccessComponent(ITextComponent component)
	{
		source.sendSuccess(component, false);
	}

	@Override public void sendFailure(String component, Object... args)
	{
		source.sendFailure(Utils.getTranslatableComponent(source.getEntity(), component, args));
	}

	@Override public void sendException(Exception exception, String component, Object... args)
	{
		sendFailure(component, args);
		Utils.exception(exception, Utils.stringFromComponent(component, args));
	}

	public @Nullable CommandInfo getFinalCommandInfo()
	{
		CommandContext<CommandSource> tempCtx = ctx;
		while (true)
		{
			String command = CommandUtils.getNode(tempCtx.getNodes(), 0);
			if (command != null && (command.equals("mocap") || command.equals("mocap:mocap"))) { return new CommandInfo(tempCtx); }

			tempCtx = ctx.getChild();
			if (tempCtx == null) { return null; }
		}
	}

	public @Nullable String getNode(int pos)
	{
		return CommandUtils.getNode(ctx.getNodes(), pos);
	}

	public int getInteger(String name)
	{
		return IntegerArgumentType.getInteger(ctx, name);
	}

	public double getDouble(String name)
	{
		return DoubleArgumentType.getDouble(ctx, name);
	}

	public boolean getBool(String name)
	{
		return BoolArgumentType.getBool(ctx, name);
	}

	public String getString(String name)
	{
		return StringArgumentType.getString(ctx, name);
	}

	public Collection<GameProfile> getGameProfiles(String name) throws CommandSyntaxException
	{
		return GameProfileArgument.getGameProfiles(ctx, name);
	}

	public @Nullable String getNullableString(String name)
	{
		try { return StringArgumentType.getString(ctx, name); }
		catch (Exception exception) { return null; }
	}

	public PlayerData getPlayerData()
	{
		String playerName = getNullableString("playerName");
		if (playerName == null) { return new PlayerData((String)null); }

		String fromPlayer = getNullableString("skinPlayerName");
		if (fromPlayer != null) { return new PlayerData(playerName, PlayerData.SkinSource.FROM_PLAYER, fromPlayer); }

		String fromFile = getNullableString("skinFilename");
		if (fromFile != null) { return new PlayerData(playerName, PlayerData.SkinSource.FROM_FILE, fromFile); }

		String fromMineskin = getNullableString("mineskinURL");
		if (fromMineskin != null) { return new PlayerData(playerName, PlayerData.SkinSource.FROM_MINESKIN, fromMineskin); }

		return new PlayerData(playerName);
	}
}
