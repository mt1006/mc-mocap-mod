package com.mt1006.mocap.command.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mt1006.mocap.mocap.settings.Settings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class SettingsCommand
{
	public static LiteralArgumentBuilder<CommandSourceStack> getArgumentBuilder()
	{
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands.literal("settings");

		//TODO: auto-adding
		commandBuilder.then(settingArgument("playingSpeed", DoubleArgumentType.doubleArg(0.0)));
		commandBuilder.then(settingArgument("recordingSync", BoolArgumentType.bool()));
		commandBuilder.then(settingArgument("playBlockActions", BoolArgumentType.bool()));
		commandBuilder.then(settingArgument("setBlockStates", BoolArgumentType.bool()));
		commandBuilder.then(settingArgument("allowMineskinRequests", BoolArgumentType.bool()));
		commandBuilder.then(settingArgument("canPushEntities", BoolArgumentType.bool()));
		commandBuilder.then(settingArgument("useCreativeGameMode", BoolArgumentType.bool()));
		commandBuilder.then(settingArgument("dropFromBlocks", BoolArgumentType.bool()));
		commandBuilder.then(settingArgument("trackVehicleEntities", BoolArgumentType.bool()));
		commandBuilder.then(settingArgument("trackItemEntities", BoolArgumentType.bool()));
		commandBuilder.then(settingArgument("trackOtherEntities", BoolArgumentType.bool()));
		commandBuilder.then(settingArgument("trackPlayedEntities", BoolArgumentType.bool()));
		commandBuilder.then(settingArgument("entityTrackingDistance", DoubleArgumentType.doubleArg(-1.0)));
		commandBuilder.then(settingArgument("playVehicleEntities", BoolArgumentType.bool()));
		commandBuilder.then(settingArgument("playItemEntities", BoolArgumentType.bool()));
		commandBuilder.then(settingArgument("playOtherEntities", BoolArgumentType.bool()));
		commandBuilder.then(settingArgument("entitiesAfterPlayback", IntegerArgumentType.integer()));
		commandBuilder.then(settingArgument("preventSavingEntities", BoolArgumentType.bool()));
		commandBuilder.then(settingArgument("recordPlayerDeath", BoolArgumentType.bool()));

		return commandBuilder;
	}

	private static int set(CommandContext<CommandSourceStack> ctx)
	{
		return Settings.set(ctx) ? 1 : 0;
	}

	private static LiteralArgumentBuilder<CommandSourceStack> settingArgument(String name, ArgumentType<?> argumentType)
	{
		return Commands.literal(name).executes(Settings::info)
				.then(Commands.argument("newValue", argumentType).executes(SettingsCommand::set));
	}
}
