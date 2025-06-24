package net.mt1006.mocap.api.v1.controller;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public interface MocapPlayback
{
	String getId();

	String getSuggestedId();

	String getRootName();

	@Nullable ServerPlayer getOwner();

	boolean isHidden();

	boolean isFinished();

	void stop();
}
