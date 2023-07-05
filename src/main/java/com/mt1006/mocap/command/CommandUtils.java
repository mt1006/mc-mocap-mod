package com.mt1006.mocap.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mt1006.mocap.mocap.playing.PlayerData;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CommandUtils
{
	public static RequiredArgumentBuilder<CommandSourceStack, String> withPlayerArguments(Command<CommandSourceStack> command)
	{
		return Commands.argument("playerName", StringArgumentType.string()).executes(command)
			.then(Commands.literal("from_player").then(Commands.argument("skinPlayerName", StringArgumentType.greedyString()).executes(command)))
			.then(Commands.literal("from_file").then(Commands.argument("skinFilename", StringArgumentType.greedyString()).executes(command)))
			.then(Commands.literal("from_mineskin").then(Commands.argument("mineskinURL", StringArgumentType.greedyString()).executes(command)));
	}

	public static @Nullable String getString(CommandContext<?> ctx, String name)
	{
		try { return StringArgumentType.getString(ctx, name); }
		catch (Exception exception) { return null; }
	}

	public static PlayerData getPlayerData(CommandContext<?> ctx)
	{
		String playerName = getString(ctx, "playerName");
		if (playerName == null) { return new PlayerData((String)null); }

		String fromPlayer = getString(ctx, "skinPlayerName");
		if (fromPlayer != null) { return new PlayerData(playerName, PlayerData.SkinSource.FROM_PLAYER, fromPlayer); }

		String fromFile = getString(ctx, "skinFilename");
		if (fromFile != null) { return new PlayerData(playerName, PlayerData.SkinSource.FROM_FILE, fromFile); }

		String fromMineskin = getString(ctx, "mineskinURL");
		if (fromMineskin != null) { return new PlayerData(playerName, PlayerData.SkinSource.FROM_MINESKIN, fromMineskin); }

		return new PlayerData(playerName);
	}

	public static Command<CommandSourceStack> simpleCommand(Function<CommandSourceStack, Boolean> function)
	{
		return (ctx) -> (function.apply(ctx.getSource()) ? 1 : 0);
	}

	public static RequiredArgumentBuilder<CommandSourceStack, String> withStringArgument(BiFunction<CommandSourceStack, String, Boolean> function, String arg)
	{
		return Commands.argument(arg, StringArgumentType.string()).executes((ctx) -> stringCommand(function, ctx, arg));
	}

	public static RequiredArgumentBuilder<CommandSourceStack, String> withTwoStringArguments(PropertyDispatch.TriFunction<CommandSourceStack, String, String, Boolean> function, String arg1, String arg2)
	{
		return Commands.argument(arg1, StringArgumentType.string())
				.then(Commands.argument(arg2, StringArgumentType.string())
				.executes((ctx) -> twoStringCommand(function, ctx, arg1, arg2)));
	}

	private static int stringCommand(BiFunction<CommandSourceStack, String, Boolean> function, CommandContext<CommandSourceStack> ctx, String arg)
	{
		try
		{
			String name = StringArgumentType.getString(ctx, arg);
			return function.apply(ctx.getSource(), name) ? 1 : 0;
		}
		catch (Exception exception)
		{
			Utils.sendException(exception, ctx.getSource(), "mocap.error.unable_to_get_argument");
			return 0;
		}
	}

	private static int twoStringCommand(PropertyDispatch.TriFunction<CommandSourceStack, String, String, Boolean> function, CommandContext<CommandSourceStack> ctx, String arg1, String arg2)
	{
		try
		{
			String name1 = StringArgumentType.getString(ctx, arg1);
			String name2 = StringArgumentType.getString(ctx, arg2);
			return function.apply(ctx.getSource(), name1, name2) ? 1 : 0;
		}
		catch (Exception exception)
		{
			Utils.sendException(exception, ctx.getSource(), "mocap.error.unable_to_get_argument");
			return 0;
		}
	}

	public static <T> @Nullable CommandContext<T> getFinalCommandContext(CommandContext<T> ctx)
	{
		while (true)
		{
			String command = getCommandNode(ctx, 0);
			if (command != null && (command.equals("mocap") || command.equals("mocap:mocap"))) { return ctx; }

			ctx = ctx.getChild();
			if (ctx == null) { return null; }
		}
	}

	public static <T> @Nullable CommandContextBuilder<T> getFinalCommandContext(CommandContextBuilder<T> ctx)
	{
		while (true)
		{
			String command = getCommandNode(ctx, 0);
			if (command != null && (command.equals("mocap") || command.equals("mocap:mocap"))) { return ctx; }

			ctx = ctx.getChild();
			if (ctx == null) { return null; }
		}
	}

	public static @Nullable String getCommandNode(CommandContext<?> ctx, int pos)
	{
		return getCommandNode(ctx.getNodes(), pos);
	}

	public static @Nullable String getCommandNode(CommandContextBuilder<?> ctx, int pos)
	{
		return getCommandNode(ctx.getNodes(), pos);
	}

	private static @Nullable String getCommandNode(List<? extends ParsedCommandNode<?>> nodes, int pos)
	{
		int size = nodes.size();
		if (pos < 0) { pos += size; }
		if (pos >= size || pos < 0) { return null; }
		return nodes.get(pos).getNode().getName();
	}
}
