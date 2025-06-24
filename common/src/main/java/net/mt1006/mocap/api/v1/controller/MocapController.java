package net.mt1006.mocap.api.v1.controller;

import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.api.v1.controller.playable.MocapActiveRecording;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MocapController
{
	@Nullable MocapPlayback findPlayback(String id);

	List<? extends MocapPlayback> getActivePlaybacks();

	@Nullable MocapActiveRecording startRecording(ServerPlayer player);
}
