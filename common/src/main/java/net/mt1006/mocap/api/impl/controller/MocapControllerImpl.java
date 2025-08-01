package net.mt1006.mocap.api.impl.controller;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.api.impl.controller.playable.MocapActiveRecordingImpl;
import net.mt1006.mocap.api.impl.controller.playable.MocapPlayableImpl;
import net.mt1006.mocap.api.v1.controller.MocapController;
import net.mt1006.mocap.api.v1.controller.MocapPlayback;
import net.mt1006.mocap.api.v1.controller.config.MocapRecordingConfig;
import net.mt1006.mocap.api.v1.controller.playable.MocapActiveRecording;
import net.mt1006.mocap.api.v1.controller.playable.MocapPlayable;
import net.mt1006.mocap.command.io.BasicCommandInfo;
import net.mt1006.mocap.mocap.playing.PlaybackManager;
import net.mt1006.mocap.mocap.recording.Recording;
import net.mt1006.mocap.mocap.recording.RecordingContext;
import net.mt1006.mocap.mocap.recording.RecordingSource;
import net.mt1006.mocap.mocap.settings.SettingFields;
import net.mt1006.mocap.mocap.settings.Settings;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MocapControllerImpl implements MocapController
{
	public final BasicCommandInfo commandInfo;
	private final RecordingSource recordingSource;
	public final boolean hideStuff;

	public MocapControllerImpl(String name, ServerLevel level, boolean hideStuff)
	{
		this.commandInfo = new BasicCommandInfo(level, name);
		this.recordingSource = RecordingSource.forAPI(name);
		this.hideStuff = hideStuff;
	}

	@Override public @Nullable MocapPlayable findPlayable(String name)
	{
		MocapPlayable playable = MocapPlayableImpl.fromName(this, name);
		return playable.exists() ? playable : null;
	}

	@Override public @Nullable MocapPlayback findPlayback(String id)
	{
		return PlaybackManager.findPlayback(commandInfo, id, null);
	}

	@Override public List<? extends MocapPlayback> getActivePlaybacks()
	{
		return List.copyOf(PlaybackManager.playbacks);
	}

	@Override public @Nullable MocapActiveRecording startRecording(ServerPlayer player)
	{
		return startRecording(player, MocapRecordingConfig.createFromSettings(), true);
	}

	@Override public @Nullable MocapActiveRecording startRecording(ServerPlayer player, MocapRecordingConfig config, boolean startInstantly)
	{
		RecordingContext ctx = Recording.start(player, recordingSource, config, null, startInstantly, false);
		return new MocapActiveRecordingImpl(this, ctx);
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
