package net.mt1006.mocap.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.mt1006.mocap.command.CommandUtils;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.settings.SettingFields;
import net.mt1006.mocap.mocap.settings.SettingGroups;
import net.mt1006.mocap.mocap.settings.Settings;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class SettingsCommand
{
	private static final Command<CommandSourceStack> COMMAND_INFO = CommandUtils.command(Settings::info);
	private static final Command<CommandSourceStack> COMMAND_SET = CommandUtils.command(SettingsCommand::set);

	public static LiteralArgumentBuilder<CommandSourceStack> getArgumentBuilder()
	{
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands.literal("settings");

		for (SettingGroups.Group group : Settings.getGroups())
		{
			LiteralArgumentBuilder<CommandSourceStack> groupBuilder = Commands.literal(group.name);
			addSettingArguments(groupBuilder, group.fields);
			commandBuilder.then(groupBuilder);
		}

		return commandBuilder;
	}

	private static boolean set(FullCommandInfo info)
	{
		return Settings.set(info);
	}

	private static void addSettingArguments(LiteralArgumentBuilder<CommandSourceStack> builder, Collection<SettingFields.Field<?>> fields)
	{
		fields.forEach((f) -> builder.then(settingArgument(f.name, f.getArgumentType(), f.getSuggestionProvider())));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> settingArgument(String name, ArgumentType<?> argumentType,
																			  @Nullable SuggestionProvider<CommandSourceStack> suggestionProvider)
	{
		RequiredArgumentBuilder<CommandSourceStack, ?> arg = Commands.argument("new_value", argumentType);
		if (suggestionProvider != null) { arg.suggests(suggestionProvider); }
		return Commands.literal(name).executes(COMMAND_INFO).then(arg.executes(COMMAND_SET));
	}
}
