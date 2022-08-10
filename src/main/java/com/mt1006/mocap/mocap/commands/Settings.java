package com.mt1006.mocap.mocap.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mt1006.mocap.utils.FileUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.HashMap;

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
				return new TranslatableComponent("mocap.commands.settings.list.info_def", name, val.toString());
			}
			else
			{
				return new TranslatableComponent("mocap.commands.settings.list.info", name, val.toString());
			}
		}
	}

	public static final Setting<Double> PLAYING_SPEED = new Setting<>("playingSpeed", 1.0);
	public static final Setting<Boolean> RECORDING_MODE = new Setting<>("recordingMode", false);
	public static HashMap<String, Setting> settingsMap = new HashMap<>();

	public static void load()
	{
		settingsMap.put(PLAYING_SPEED.name, PLAYING_SPEED);
		settingsMap.put(RECORDING_MODE.name, RECORDING_MODE);
	}

	public static void save()
	{

	}

	public static int list(CommandContext<CommandSourceStack> ctx)
	{
		CommandSourceStack commandSource = ctx.getSource();

		commandSource.sendSuccess(new TranslatableComponent("mocap.commands.settings.list"), false);
		for (Setting setting : settingsMap.values())
		{
			commandSource.sendSuccess(setting.getInfo(), false);
		}

		return 1;
	}

	public static int info(CommandContext<CommandSourceStack> ctx)
	{
		CommandSourceStack commandSource = ctx.getSource();

		String settingName;
		try
		{
			settingName = ctx.getNodes().get(ctx.getNodes().size() - 1).getNode().getName();
		}
		catch (Exception exception)
		{
			ctx.getSource().sendFailure(new TranslatableComponent("mocap.commands.error.unable_to_get_argument"));
			return 0;
		}

		commandSource.sendSuccess(new TranslatableComponent("mocap.commands.settings.info.name", settingName), false);
		commandSource.sendSuccess(new TranslatableComponent("mocap.commands.settings.info.about." + settingName), false);
		commandSource.sendSuccess(new TranslatableComponent("mocap.commands.settings.info.val",
				settingsMap.get(settingName).val.toString()), false);
		commandSource.sendSuccess(new TranslatableComponent("mocap.commands.settings.info.def_val",
				settingsMap.get(settingName).defVal.toString()), false);

		return 1;
	}

	public static int set(CommandContext<CommandSourceStack> ctx)
	{
		return 1;
	}
}
