package net.mt1006.mocap.api.v1.controller.playable;

import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.controller.MocapFile;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.mocap.playing.playable.RecordingFile;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface MocapRecordingFile extends MocapPlayable, MocapFile<MocapRecordingFile>
{
	static @Nullable MocapRecordingFile get(CommandOutput out, String name)
	{
		return RecordingFile.get(out, name);
	}

	/**
	 * Return detailed information about recording file.
	 * @param out log output
	 * @return MocapRecordingFile.Info instance
	 * @see MocapRecordingFile.Info
	 */
	@Nullable Info getInfo(CommandOutput out);

	/**
	 * Data of a player profile assigned to a recording
	 */
	interface AssignedProfile
	{
		/**
		 * @return name of a player, null if not assigned
		 */
		@Nullable String name();

		/**
		 * @return UUID of a player, null if not assigned
		 */
		@Nullable UUID id();

		@Nullable String skinValue();

		@Nullable String skinSignature();
	}

	interface Info
	{
		/**
		 * @return version of a recording
		 */
		int version();

		/**
		 * @return whenever version is marked as experimental (e.g. recorded on alpha version)
		 */
		boolean experimental();

		/**
		 * @return iteration of experimental release of a specific version (subversion)
		 */
		int experimentalSubversion();

		/**
		 * @return length of a recording in ticks (1/20 of a second)
		 */
		long lengthInTicks();

		/**
		 * @return size of a recording file in bytes
		 */
		long sizeInBytes();

		/**
		 * @return size of a recording in number of actions
		 */
		long sizeInActionCount();

		/**
		 * @return first position of a recorded player
		 */
		Vec3 startPos();

		/**
		 * @return Minecraft ID of a dimension assigned to a recording, or null if it wasn't assigned
		 */
		@Nullable Identifier assignedDimensionId();

		/**
		 * @return object holding data of assigned player profile, or null if it wasn't assigned
		 */
		@Nullable AssignedProfile assignedProfile();

		/**
		 * <b>WARNING: "Ends with death" flag is no longer used by recordings (since version 1.4)!</b>
		 * On modern recordings it will return false even if recording ends with death, as Die action is now used instead.
		 * @return whenever recording should end with death
		 */
		boolean legacyEndsWithDeath();
	}
}
