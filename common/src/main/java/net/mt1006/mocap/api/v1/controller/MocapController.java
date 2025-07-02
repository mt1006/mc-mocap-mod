package net.mt1006.mocap.api.v1.controller;

import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.api.v1.controller.config.MocapRecordingConfig;
import net.mt1006.mocap.api.v1.controller.playable.MocapActiveRecording;
import net.mt1006.mocap.api.v1.controller.playable.MocapPlayable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MocapController
{
	@Nullable MocapPlayable findPlayable(String name);

	@Nullable MocapPlayback findPlayback(String id);

	List<? extends MocapPlayback> getActivePlaybacks();

	@Nullable MocapActiveRecording startRecording(ServerPlayer player);

	@Nullable MocapActiveRecording startRecording(ServerPlayer player, MocapRecordingConfig config, boolean startInstantly);

	@Nullable String getSetting(String name);

	boolean setSetting(String name, String val);

	boolean resetSetting(String name);
}
