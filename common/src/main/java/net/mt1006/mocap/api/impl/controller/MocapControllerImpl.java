package net.mt1006.mocap.api.impl.controller;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.api.v1.controller.MocapController;
import net.mt1006.mocap.api.v1.controller.MocapPlaybackRoot;
import net.mt1006.mocap.api.v1.controller.config.MocapRecordingConfig;
import net.mt1006.mocap.api.v1.controller.playable.MocapActiveRecording;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.command.io.APICommandInfo;
import net.mt1006.mocap.mocap.playing.PlaybackManager;
import net.mt1006.mocap.mocap.playing.playable.ActiveRecording;
import net.mt1006.mocap.mocap.recording.RecordingManager;
import net.mt1006.mocap.mocap.recording.RecordingSource;
import net.mt1006.mocap.mocap.settings.SettingFields;
import net.mt1006.mocap.mocap.settings.Settings;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MocapControllerImpl implements MocapController
{
	public final APICommandInfo commandInfo;
	private final RecordingSource recordingSource;
	public final boolean hideStuff;

	public MocapControllerImpl(String name, ServerLevel level, boolean hideStuff)
	{
		this.commandInfo = new APICommandInfo(level, name);
		this.recordingSource = RecordingSource.forAPI(name);
		this.hideStuff = hideStuff;
	}

	@Override public CommandInfo getCommandInfo()
	{
		return commandInfo;
	}

	@Override public @Nullable MocapPlaybackRoot findPlayback(String id)
	{
		return PlaybackManager.findPlayback(commandInfo, id, null);
	}

	@Override public List<? extends MocapPlaybackRoot> getActivePlaybacks()
	{
		return List.copyOf(PlaybackManager.playbacks);
	}

	@Override public @Nullable MocapActiveRecording startRecording(ServerPlayer player)
	{
		return startRecording(player, MocapRecordingConfig.createFromSettings(), true);
	}

	@Override public @Nullable MocapActiveRecording startRecording(ServerPlayer player, MocapRecordingConfig config, boolean startInstantly)
	{
		return ActiveRecording.get(RecordingManager.start(player, recordingSource, config, null, startInstantly, false));
	}

	@Override public @Nullable String getSetting(String name)
	{
		SettingFields.Field<?> field = Settings.getField(name);
		return field != null ? field.valToString() : null;
	}

	@Override public boolean setSetting(String name, String val)
	{
		SettingFields.Field<?> field = Settings.getField(name);
		return field != null && field.setFromString(val);
	}

	@Override public boolean resetSetting(String name)
	{
		SettingFields.Field<?> field = Settings.getField(name);
		if (field != null) { field.reset(); }
		return field != null;
	}
}
