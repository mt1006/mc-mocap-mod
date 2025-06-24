package net.mt1006.mocap.api.impl.controller;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.api.impl.controller.playable.MocapActiveRecordingImpl;
import net.mt1006.mocap.api.v1.controller.MocapController;
import net.mt1006.mocap.api.v1.controller.MocapPlayback;
import net.mt1006.mocap.api.v1.controller.playable.MocapActiveRecording;
import net.mt1006.mocap.command.io.BasicCommandInfo;
import net.mt1006.mocap.mocap.playing.Playing;
import net.mt1006.mocap.mocap.recording.Recording;
import net.mt1006.mocap.mocap.recording.RecordingContext;
import net.mt1006.mocap.mocap.recording.RecordingSource;
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

	@Override public @Nullable MocapPlayback findPlayback(String id)
	{
		return Playing.findPlayback(commandInfo, id, null);
	}

	@Override public List<? extends MocapPlayback> getActivePlaybacks()
	{
		return List.copyOf(Playing.playbacks);
	}

	@Override public @Nullable MocapActiveRecording startRecording(ServerPlayer player)
	{
		RecordingContext ctx = Recording.start(player, recordingSource, null, true, false);
		return new MocapActiveRecordingImpl(this, ctx);
	}
}
