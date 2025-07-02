package net.mt1006.mocap.api.v1.controller.playable;

import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.controller.MocapFile;
import org.jetbrains.annotations.Nullable;

public interface MocapSavedRecording extends MocapPlayable, MocapFile<MocapSavedRecording>
{
	@Nullable Info getInfo();

	interface Info
	{
		int version();

		boolean experimental();

		long lengthInTicks();

		long sizeInBytes();

		long sizeInOps();

		Vec3 startPos();

		@Nullable String assignedPlayerName();

		boolean legacyEndsWithDeath();
	}
}
