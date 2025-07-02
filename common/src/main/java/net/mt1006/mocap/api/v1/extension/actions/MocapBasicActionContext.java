package net.mt1006.mocap.api.v1.extension.actions;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.extension.MocapPositionTransformer;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;

public interface MocapBasicActionContext
{
	MocapRecordingData getRecordingData();

	Entity getEntity();

	ServerLevel getLevel();

	MocapPlaybackConfig getConfig();

	MocapModifiers getModifiers();

	MocapPositionTransformer getTransformer();
}
