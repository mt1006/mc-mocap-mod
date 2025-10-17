package net.mt1006.mocap.api.v1.controller.playable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.controller.MocapFile;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.mocap.playing.playable.RecordingFile;
import org.jetbrains.annotations.Nullable;

public interface MocapRecordingFile extends MocapPlayable, MocapFile<MocapRecordingFile>
{
	static @Nullable MocapRecordingFile get(CommandOutput out, String name)
	{
		return RecordingFile.get(out, name);
	}

	@Nullable Info getInfo(CommandOutput out);

	interface Info
	{
		int version();

		boolean experimental();

		int experimentalSubversion();

		long lengthInTicks();

		long sizeInBytes();

		long sizeInOps();

		Vec3 startPos();

		@Nullable ResourceLocation assignedDimensionId();

		@Nullable String assignedPlayerName();

		boolean legacyEndsWithDeath();
	}
}
