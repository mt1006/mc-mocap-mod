package com.mt1006.mocap.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CommandUtils
{
	public static RequiredArgumentBuilder<CommandSource, String> withPlayerArguments(Command<CommandSource> command)
	{
		return Commands.argument("playerName", StringArgumentType.string()).executes(command)
			.then(Commands.literal("from_player").then(Commands.argument("skinPlayerName", StringArgumentType.greedyString()).executes(command)))
			.then(Commands.literal("from_file").then(Commands.argument("skinFilename", StringArgumentType.greedyString()).executes(command)))
			.then(Commands.literal("from_mineskin").then(Commands.argument("mineskinURL", StringArgumentType.greedyString()).executes(command)));
	}

	public static Command<CommandSource> command(Function<CommandInfo, Boolean> function)
	{
		return (ctx) -> (function.apply(new CommandInfo(ctx)) ? 1 : 0);
	}

	public static RequiredArgumentBuilder<CommandSource, String> withStringArgument(BiFunction<CommandInfo, String, Boolean> function, String arg)
	{
		return Commands.argument(arg, StringArgumentType.string()).executes((ctx) -> stringCommand(function, ctx, arg));
	}

	public static RequiredArgumentBuilder<CommandSource, String> withTwoStringArguments(TriFunction<CommandInfo, String, String, Boolean> function, String arg1, String arg2)
	{
		return Commands.argument(arg1, StringArgumentType.string())
				.then(Commands.argument(arg2, StringArgumentType.string())
				.executes((ctx) -> twoStringCommand(function, ctx, arg1, arg2)));
	}

	private static int stringCommand(BiFunction<CommandInfo, String, Boolean> function, CommandContext<CommandSource> ctx, String arg)
	{
		CommandInfo commandInfo = new CommandInfo(ctx);
		try
		{
			String name = StringArgumentType.getString(ctx, arg);
			return function.apply(commandInfo, name) ? 1 : 0;
		}
		catch (Exception exception)
		{
			commandInfo.sendException(exception, "mocap.error.unable_to_get_argument");
			return 0;
		}
	}

	private static int twoStringCommand(TriFunction<CommandInfo, String, String, Boolean> function, CommandContext<CommandSource> ctx, String arg1, String arg2)
	{
		CommandInfo commandInfo = new CommandInfo(ctx);
		try
		{
			String name1 = StringArgumentType.getString(ctx, arg1);
			String name2 = StringArgumentType.getString(ctx, arg2);
			return function.apply(commandInfo, name1, name2) ? 1 : 0;
		}
		catch (Exception exception)
		{
			commandInfo.sendException(exception, "mocap.error.unable_to_get_argument");
			return 0;
		}
	}

	public static <T> @Nullable CommandContextBuilder<T> getFinalCommandContext(CommandContextBuilder<T> ctx)
	{
		while (true)
		{
			String command = getNode(ctx, 0);
			if (command != null && (command.equals("mocap") || command.equals("mocap:mocap"))) { return ctx; }

			ctx = ctx.getChild();
			if (ctx == null) { return null; }
		}
	}

	public static @Nullable String getNode(CommandContextBuilder<?> ctx, int pos)
	{
		return getNode(ctx.getNodes(), pos);
	}

	public static @Nullable String getNode(List<? extends ParsedCommandNode<?>> nodes, int pos)
	{
		int size = nodes.size();
		if (pos < 0) { pos += size; }
		if (pos >= size || pos < 0) { return null; }
		return nodes.get(pos).getNode().getName();
	}

	public interface TriFunction<T, U, V, R>
	{
		R apply(T t, U u, V v);
	}
}
