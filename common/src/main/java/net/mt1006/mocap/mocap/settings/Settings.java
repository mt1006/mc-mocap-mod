package net.mt1006.mocap.mocap.settings;

import net.mt1006.mocap.api.v1.controller.config.MocapDimensionSource;
import net.mt1006.mocap.api.v1.controller.config.MocapEntitiesAfterPlayback;
import net.mt1006.mocap.api.v1.controller.config.MocapOnChangeDimension;
import net.mt1006.mocap.api.v1.controller.config.MocapOnDeath;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.playing.modifiers.EntityFilter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class Settings
{
	private static final SettingFields fields = new SettingFields();
	private static final SettingGroups groups = new SettingGroups();

	private static final SettingGroups.Group RECORDING = groups.add("recording");
	private static final SettingGroups.Group PLAYBACK = groups.add("playback");
	private static final SettingGroups.Group ADVANCED = groups.add("advanced");

	public static final SettingFields.EntityFilterField TRACK_ENTITIES = RECORDING.add(fields.addFilterField("track_entities", "@vehicles;@projectiles;@items", EntityFilter::onTrackEntitiesSet));
	static final SettingFields.BooleanField PREVENT_TRACKING_PLAYED_ENTITIES = RECORDING.add(fields.add("prevent_tracking_played_entities", true));
	static final SettingFields.DoubleField ENTITY_TRACKING_DISTANCE = RECORDING.add(fields.add("entity_tracking_distance", 128.0));
	static final SettingFields.EnumField<MocapOnDeath> ON_DEATH = RECORDING.add(fields.add("on_death", MocapOnDeath.END_RECORDING));
	static final SettingFields.EnumField<MocapOnChangeDimension> ON_CHANGE_DIMENSION = RECORDING.add(fields.add("on_change_dimension", MocapOnChangeDimension.END_RECORDING));
	public static final SettingFields.BooleanField START_INSTANTLY = RECORDING.add(fields.add("start_instantly", false));
	static final SettingFields.BooleanField ASSIGN_DIMENSION = RECORDING.add(fields.add("assign_dimension", true));
	static final SettingFields.BooleanField ASSIGN_PLAYER_NAME = RECORDING.add(fields.add("assign_player_name", false));
	static final SettingFields.BooleanField CHAT_RECORDING = RECORDING.add(fields.add("chat_recording", false));

	public static final SettingFields.DoubleField PLAYBACK_SPEED = PLAYBACK.add(fields.add("playback_speed", 1.0));
	public static final SettingFields.EntityFilterField PLAY_ENTITIES = PLAYBACK.add(fields.addFilterField("play_entities", "*", EntityFilter::onPlaybackEntitiesSet));
	static final SettingFields.BooleanField CAN_PUSH_ENTITIES = PLAYBACK.add(fields.add("can_push_entities", true));
	static final SettingFields.EnumField<MocapEntitiesAfterPlayback> ENTITIES_AFTER_PLAYBACK = PLAYBACK.add(fields.add("entities_after_playback", MocapEntitiesAfterPlayback.REMOVE));
	static final SettingFields.BooleanField BLOCK_ACTIONS_PLAYBACK = PLAYBACK.add(fields.add("block_actions_playback", true));
	static final SettingFields.BooleanField BLOCK_INITIALIZATION = PLAYBACK.add(fields.add("block_initialization", true));
	static final SettingFields.BooleanField BLOCK_ALLOW_SCALED = PLAYBACK.add(fields.add("block_allow_scaled", false));
	static final SettingFields.BooleanField DROP_FROM_BLOCKS = PLAYBACK.add(fields.add("drop_from_blocks", false));
	static final SettingFields.BooleanField START_AS_RECORDED = PLAYBACK.add(fields.add("start_as_recorded", false));
	static final SettingFields.BooleanField CHAT_PLAYBACK = PLAYBACK.add(fields.add("chat_playback", true));
	static final SettingFields.BooleanField INVULNERABLE_PLAYBACK = PLAYBACK.add(fields.add("invulnerable_playback", true));
	public static final SettingFields.EnumField<MocapDimensionSource> DIMENSION_SOURCE = PLAYBACK.add(fields.add("dimension_source", MocapDimensionSource.ASSIGNED_OR_CURRENT));

	public static final SettingFields.DoubleField FLUENT_MOVEMENTS = ADVANCED.add(fields.add("fluent_movements", 32.0));
	public static final SettingFields.DoubleField MAX_FLOAT_POS_VALUE = ADVANCED.add(fields.add("max_float_pos_value", 1024.0));
	//public static final SettingFields.BooleanField PRECISE_ROTATION_RECORDING = ADVANCED.add(fields.add("precise_rotation_recording", true));
	//public static final SettingFields.BooleanField PRECISE_ROTATION_PLAYBACK = ADVANCED.add(fields.add("precise_rotation_playback", false)); //TODO: restore?
	public static final SettingFields.BooleanField ALLOW_MINESKIN_REQUESTS = ADVANCED.add(fields.add("allow_mineskin_requests", true));
	public static final SettingFields.BooleanField PREVENT_SAVING_ENTITIES = ADVANCED.add(fields.add("prevent_saving_entities", true));
	public static final SettingFields.BooleanField USE_CREATIVE_GAME_MODE = ADVANCED.add(fields.add("use_creative_game_mode", false));
	public static final SettingFields.BooleanField ALLOW_GHOSTS = ADVANCED.add(fields.add("allow_ghosts", true));
	public static final SettingFields.BooleanField EXPERIMENTAL_RELEASE_WARNING = ADVANCED.add(fields.add("experimental_release_warning", true));
	public static final SettingFields.BooleanField PRETTY_SCENE_FILES = ADVANCED.add(fields.add("pretty_scene_files", true));
	public static final SettingFields.BooleanField SHOW_TIPS = ADVANCED.add(fields.add("show_tips", true));

	public static void save()
	{
		fields.save();
	}

	public static void load()
	{
		fields.load();
	}

	public static void unload()
	{
		fields.unload();
	}

	public static Collection<SettingFields.Field<?>> getFields()
	{
		return fields.fieldMap.values();
	}

	public static @Nullable SettingFields.Field<?> getField(String name)
	{
		return fields.fieldMap.get(name);
	}

	public static Collection<SettingGroups.Group> getGroups()
	{
		return groups.groupMap.values();
	}

	public static boolean list(FullCommandInfo info)
	{
		info.sendSuccess("settings.list");
		getFields().forEach((f) -> info.sendSuccessComponent(f.getInfo(info)));
		return true;
	}

	public static boolean info(FullCommandInfo info)
	{
		String settingName = info.getNode(-1);
		if (settingName == null)
		{
			info.sendFailure("error.unable_to_get_argument");
			return false;
		}

		SettingFields.Field<?> field = fields.fieldMap.get(settingName);
		if (field == null)
		{
			info.sendFailure("settings.error");
			return false;
		}

		info.sendSuccess("settings.info.name", settingName);
		info.sendSuccess("settings.info.about", info.getTranslatableComponent("settings.info.about." + settingName));
		field.printValues(info);
		return true;
	}

	public static boolean set(FullCommandInfo info)
	{
		String settingName = info.getNode(-2);
		if (settingName == null)
		{
			info.sendFailure("error.unable_to_get_argument");
			return false;
		}

		SettingFields.Field<?> field = fields.fieldMap.get(settingName);
		if (field == null)
		{
			info.sendFailure("settings.error");
			return false;
		}

		String oldValue = field.valToString();
		if (!field.fromCommand(info)) { return false; }

		String newValue = field.valToString();
		oldValue = oldValue.isEmpty() ? "[empty]" : oldValue;
		newValue = newValue.isEmpty() ? "[empty]" : newValue;

		if (oldValue.equals(newValue)) { info.sendSuccess("settings.set.success.not_changed", newValue); }
		else { info.sendSuccess("settings.set.success.changed", oldValue, newValue); }
		save();
		return true;
	}
}
