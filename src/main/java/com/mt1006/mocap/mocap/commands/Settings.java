package com.mt1006.mocap.mocap.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mt1006.mocap.utils.FileUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

public class Settings
{
	public static class Setting<T>
	{
		public String name;
		public T defVal;
		public T val;

		public Setting(String name, T defVal)
		{
			this.name = name;
			this.defVal = defVal;
			this.val = defVal;
		}

		public Component getInfo()
		{
			if (val.equals(defVal))
			{
				return Component.translatable("mocap.commands.settings.list.info_def", name, val.toString());
			}
			else
			{
				return Component.translatable("mocap.commands.settings.list.info", name, val.toString());
			}
		}

		public void fromString(String str)
		{
			try
			{
				if (defVal instanceof Double) { val = (T)new Double(Double.parseDouble(str)); }
				else if (defVal instanceof Boolean) { val = (T)new Boolean(Boolean.parseBoolean(str)); }
				else { val = defVal; }
			}
			catch (Exception exception)
			{
				val = defVal;
			}
		}

		public boolean fromCommand(CommandContext<CommandSourceStack> ctx)
		{
			try
			{
				if (defVal instanceof Double) { val = (T)new Double(DoubleArgumentType.getDouble(ctx, "newValue")); }
				else if (defVal instanceof Boolean) { val = (T)new Boolean(BoolArgumentType.getBool(ctx, "newValue")); }
				else { val = defVal; }
				return true;
			}
			catch (Exception exception)
			{
				val = defVal;
				return false;
			}
		}

		@Nullable
		public String getString()
		{
			StringBuilder str = new StringBuilder(name);
			str.append("=");

			try
			{
				if (val instanceof Double) { str.append(val); }
				else if (val instanceof Boolean) { str.append(val); }
				else { return null; }
			}
			catch (Exception exception)
			{
				return null;
			}

			str.append("\n");
			return new String(str);
		}
	}

	public static final Setting<Double> PLAYING_SPEED = new Setting<>("playingSpeed", 1.0);
	public static final Setting<Boolean> RECORDING_SYNC = new Setting<>("recordingSync", false);
	public static HashMap<String, Setting<?>> settingsMap = new HashMap<>();
	public static boolean loaded = false;

	public static void load(CommandSourceStack commandSource)
	{
		settingsMap.put(PLAYING_SPEED.name, PLAYING_SPEED);
		settingsMap.put(RECORDING_SYNC.name, RECORDING_SYNC);

		try
		{
			File settingsFile = FileUtils.getSettingsFile(commandSource);

			if (settingsFile != null)
			{
				Scanner fileScanner = new Scanner(settingsFile);

				while (fileScanner.hasNextLine())
				{
					String line = fileScanner.nextLine();
					if (line.length() == 0) { continue; }
					String[] parts = line.split("=");

					if (parts.length != 2)
					{
						fileScanner.close();
						return;
					}

					Setting<?> setting = settingsMap.get(parts[0]);
					if (setting == null)
					{
						fileScanner.close();
						return;
					}

					setting.fromString(parts[1]);
				}

				fileScanner.close();
			}
		}
		catch (Exception ignore) {}

		loaded = true;
	}

	public static void save(CommandSourceStack commandSource)
	{
		try
		{
			File settingsFile = FileUtils.getSettingsFile(commandSource);
			if (settingsFile == null) { return; }

			PrintWriter printWriter = new PrintWriter(settingsFile);

			for (Setting<?> setting : settingsMap.values())
			{
				String str = setting.getString();
				if (str != null) { printWriter.print(str); }
			}

			printWriter.close();
		}
		catch (Exception ignore) {}
	}

	public static void unload()
	{
		settingsMap.clear();
		loaded = false;
	}

	public static int list(CommandContext<CommandSourceStack> ctx)
	{
		CommandSourceStack commandSource = ctx.getSource();
		if (!loaded) { load(commandSource); }

		commandSource.sendSuccess(Component.translatable("mocap.commands.settings.list"), false);
		for (Setting<?> setting : settingsMap.values())
		{
			commandSource.sendSuccess(setting.getInfo(), false);
		}

		return 1;
	}

	public static int info(CommandContext<CommandSourceStack> ctx)
	{
		CommandSourceStack commandSource = ctx.getSource();
		if (!loaded) { load(commandSource); }

		String settingName;
		try
		{
			settingName = ctx.getNodes().get(ctx.getNodes().size() - 1).getNode().getName();
		}
		catch (Exception exception)
		{
			ctx.getSource().sendFailure(Component.translatable("mocap.commands.error.unable_to_get_argument"));
			return 0;
		}

		Setting<?> setting = settingsMap.get(settingName);
		if (setting == null)
		{
			commandSource.sendFailure(Component.translatable("mocap.commands.settings.error"));
			return 0;
		}

		commandSource.sendSuccess(Component.translatable("mocap.commands.settings.info.name", settingName), false);
		commandSource.sendSuccess(Component.translatable("mocap.commands.settings.info.about." + settingName), false);
		commandSource.sendSuccess(Component.translatable("mocap.commands.settings.info.val", setting.val.toString()), false);
		commandSource.sendSuccess(Component.translatable("mocap.commands.settings.info.def_val", setting.defVal.toString()), false);

		return 1;
	}

	public static int set(CommandContext<CommandSourceStack> ctx)
	{
		CommandSourceStack commandSource = ctx.getSource();
		if (!loaded) { load(commandSource); }

		String settingName;
		try
		{
			settingName = ctx.getNodes().get(ctx.getNodes().size() - 2).getNode().getName();
		}
		catch (Exception exception)
		{
			ctx.getSource().sendFailure(Component.translatable("mocap.commands.error.unable_to_get_argument"));
			return 0;
		}

		Setting<?> setting = settingsMap.get(settingName);
		if (setting == null)
		{
			commandSource.sendFailure(Component.translatable("mocap.commands.settings.error"));
			return 0;
		}

		String oldValue = setting.val.toString();
		if (!setting.fromCommand(ctx))
		{
			commandSource.sendFailure(Component.translatable("mocap.commands.settings.set.error"));
		}

		commandSource.sendSuccess(Component.translatable("mocap.commands.settings.set", oldValue, setting.val.toString()), false);
		save(commandSource);
		return 1;
	}
}
