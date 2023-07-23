package com.mt1006.mocap.mocap.settings;

import com.mt1006.mocap.command.CommandInfo;
import com.mt1006.mocap.mocap.files.Files;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class Settings
{
	public static final Setting<Double> PLAYING_SPEED = new Setting<>("playingSpeed", 1.0);
	public static final Setting<Boolean> RECORDING_SYNC = new Setting<>("recordingSync", false);
	public static final Setting<Boolean> PLAY_BLOCK_ACTIONS = new Setting<>("playBlockActions", true);
	public static final Setting<Boolean> SET_BLOCK_STATES = new Setting<>("setBlockStates", true);
	public static final Setting<Boolean> ALLOW_MINESKIN_REQUESTS = new Setting<>("allowMineskinRequests", true);
	public static final Setting<Boolean> CAN_PUSH_ENTITIES = new Setting<>("canPushEntities", true);
	public static final Setting<Boolean> USE_CREATIVE_GAME_MODE = new Setting<>("useCreativeGameMode", false);
	public static final Setting<Boolean> DROP_FROM_BLOCKS = new Setting<>("dropFromBlocks", false);
	public static final Setting<Boolean> TRACK_VEHICLE_ENTITIES = new Setting<>("trackVehicleEntities", true);
	public static final Setting<Boolean> TRACK_ITEM_ENTITIES = new Setting<>("trackItemEntities", true);
	public static final Setting<Boolean> TRACK_OTHER_ENTITIES = new Setting<>("trackOtherEntities", false);
	public static final Setting<Boolean> TRACK_PLAYED_ENTITIES = new Setting<>("trackPlayedEntities", false);
	public static final Setting<Double> ENTITY_TRACKING_DISTANCE = new Setting<>("entityTrackingDistance", 128.0);
	public static final Setting<Boolean> PLAY_VEHICLE_ENTITIES = new Setting<>("playVehicleEntities", true);
	public static final Setting<Boolean> PLAY_ITEM_ENTITIES = new Setting<>("playItemEntities", true);
	public static final Setting<Boolean> PLAY_OTHER_ENTITIES = new Setting<>("playOtherEntities", true);
	public static final Setting<Integer> ENTITIES_AFTER_PLAYBACK = new Setting<>("entitiesAfterPlayback", 1);
	public static final Setting<Boolean> PREVENT_SAVING_ENTITIES = new Setting<>("preventSavingEntities", true);
	public static final Setting<Boolean> RECORD_PLAYER_DEATH = new Setting<>("recordPlayerDeath", true);
	public static final Setting<Double> FLUENT_MOVEMENTS = new Setting<>("fluentMovements", 32.0);
	//TODO: add projectiles tracking/playing setting

	public static Map<String, Setting<?>> settingsMap = new LinkedHashMap<>();
	public static boolean loaded = false;

	public static void load(CommandInfo commandInfo)
	{
		settingsMap.put(PLAYING_SPEED.name, PLAYING_SPEED);
		settingsMap.put(RECORDING_SYNC.name, RECORDING_SYNC);
		settingsMap.put(PLAY_BLOCK_ACTIONS.name, PLAY_BLOCK_ACTIONS);
		settingsMap.put(SET_BLOCK_STATES.name, SET_BLOCK_STATES);
		settingsMap.put(ALLOW_MINESKIN_REQUESTS.name, ALLOW_MINESKIN_REQUESTS);
		settingsMap.put(CAN_PUSH_ENTITIES.name, CAN_PUSH_ENTITIES);
		settingsMap.put(USE_CREATIVE_GAME_MODE.name, USE_CREATIVE_GAME_MODE);
		settingsMap.put(DROP_FROM_BLOCKS.name, DROP_FROM_BLOCKS);
		settingsMap.put(TRACK_VEHICLE_ENTITIES.name, TRACK_VEHICLE_ENTITIES);
		settingsMap.put(TRACK_ITEM_ENTITIES.name, TRACK_ITEM_ENTITIES);
		settingsMap.put(TRACK_OTHER_ENTITIES.name, TRACK_OTHER_ENTITIES);
		settingsMap.put(TRACK_PLAYED_ENTITIES.name, TRACK_PLAYED_ENTITIES);
		settingsMap.put(ENTITY_TRACKING_DISTANCE.name, ENTITY_TRACKING_DISTANCE);
		settingsMap.put(PLAY_VEHICLE_ENTITIES.name, PLAY_VEHICLE_ENTITIES);
		settingsMap.put(PLAY_ITEM_ENTITIES.name, PLAY_ITEM_ENTITIES);
		settingsMap.put(PLAY_OTHER_ENTITIES.name, PLAY_OTHER_ENTITIES);
		settingsMap.put(ENTITIES_AFTER_PLAYBACK.name, ENTITIES_AFTER_PLAYBACK);
		settingsMap.put(PREVENT_SAVING_ENTITIES.name, PREVENT_SAVING_ENTITIES);
		settingsMap.put(RECORD_PLAYER_DEATH.name, RECORD_PLAYER_DEATH);
		settingsMap.put(FLUENT_MOVEMENTS.name, FLUENT_MOVEMENTS);

		settingsMap.values().forEach(Setting::reset);

		try
		{
			File settingsFile = Files.getSettingsFile(commandInfo);

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

	public static void save(CommandInfo commandInfo)
	{
		try
		{
			File settingsFile = Files.getSettingsFile(commandInfo);
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

	public static boolean list(CommandInfo commandInfo)
	{
		if (!loaded) { load(commandInfo); }

		commandInfo.sendSuccess("mocap.settings.list");
		for (Setting<?> setting : settingsMap.values())
		{
			commandInfo.sendSuccessComponent(setting.getInfo(commandInfo));
		}
		return true;
	}

	public static boolean info(CommandInfo commandInfo)
	{
		if (!loaded) { load(commandInfo); }

		String settingName;
		try
		{
			settingName = commandInfo.ctx.getNodes().get(commandInfo.ctx.getNodes().size() - 1).getNode().getName();
		}
		catch (Exception exception)
		{
			commandInfo.sendException(exception, "mocap.error.unable_to_get_argument");
			return false;
		}

		Setting<?> setting = settingsMap.get(settingName);
		if (setting == null)
		{
			commandInfo.sendFailure("mocap.settings.error");
			return false;
		}

		commandInfo.sendSuccess("mocap.settings.info.name", settingName);
		commandInfo.sendSuccess("mocap.settings.info.about." + settingName);
		commandInfo.sendSuccess("mocap.settings.info.val", setting.val.toString());
		commandInfo.sendSuccess("mocap.settings.info.def_val", setting.defVal.toString());
		return true;
	}

	public static boolean set(CommandInfo commandInfo)
	{
		if (!loaded) { load(commandInfo); }

		String settingName = commandInfo.getNode(-2);
		if (settingName == null)
		{
			commandInfo.sendFailure("mocap.error.unable_to_get_argument");
			return false;
		}

		Setting<?> setting = settingsMap.get(settingName);
		if (setting == null)
		{
			commandInfo.sendFailure("mocap.settings.error");
			return false;
		}

		String oldValue = setting.val.toString();
		if (!setting.fromCommand(commandInfo))
		{
			commandInfo.sendFailure("mocap.settings.set.error");
		}

		commandInfo.sendSuccess("mocap.settings.set", oldValue, setting.val.toString());
		save(commandInfo);
		return true;
	}

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

		public void reset()
		{
			val = defVal;
		}

		public Component getInfo(CommandInfo commandInfo)
		{
			if (val.equals(defVal))
			{
				return Utils.getTranslatableComponent(commandInfo.source.getEntity(), "mocap.settings.list.info_def", name, val.toString());
			}
			else
			{
				return Utils.getTranslatableComponent(commandInfo.source.getEntity(), "mocap.settings.list.info", name, val.toString());
			}
		}

		public void fromString(String str)
		{
			try
			{
				if (defVal instanceof Double) { val = (T)Double.valueOf(Double.parseDouble(str)); }
				else if (defVal instanceof Integer) { val = (T)Integer.valueOf(Integer.parseInt(str)); }
				else if (defVal instanceof Boolean) { val = (T)Boolean.valueOf(Boolean.parseBoolean(str)); }
				else { throw new RuntimeException(); }
			}
			catch (Exception exception)
			{
				Utils.exception(exception, "Failed to load settings from string");
				val = defVal;
			}
		}

		public boolean fromCommand(CommandInfo commandInfo)
		{
			try
			{
				if (defVal instanceof Double) { val = (T)Double.valueOf(commandInfo.getDouble("newValue")); }
				else if (defVal instanceof Integer) { val = (T)Integer.valueOf(commandInfo.getInteger("newValue")); }
				else if (defVal instanceof Boolean) { val = (T)Boolean.valueOf(commandInfo.getBool("newValue")); }
				else { throw new RuntimeException(); }
				return true;
			}
			catch (Exception exception)
			{
				Utils.exception(exception, "Failed to load settings from command");
				val = defVal;
				return false;
			}
		}

		public @Nullable String getString()
		{
			StringBuilder str = new StringBuilder(name);
			str.append("=");

			try
			{
				if (val instanceof Double) { str.append(val); }
				else if (val instanceof Integer) { str.append(val); }
				else if (val instanceof Boolean) { str.append(val); }
				else { throw new RuntimeException(); }
			}
			catch (Exception exception)
			{
				Utils.exception(exception, "Failed to save settings");
				return null;
			}

			str.append("\n");
			return new String(str);
		}
	}
}
