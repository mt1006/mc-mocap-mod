package net.mt1006.mocap.mocap.playing.playable;

import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.controller.MocapPlaybackRoot;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.controller.playable.MocapRecordingFile;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.mocap.files.Files;
import net.mt1006.mocap.mocap.files.RecordingData;
import net.mt1006.mocap.mocap.files.RecordingFiles;
import net.mt1006.mocap.mocap.playing.PlaybackDataManager;
import net.mt1006.mocap.mocap.playing.PlaybackManager;
import net.mt1006.mocap.mocap.playing.playback.Playback;
import net.mt1006.mocap.mocap.playing.playback.PositionTransformer;
import net.mt1006.mocap.mocap.playing.playback.RecordingPlayback;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class RecordingFile extends PlayableFile<MocapRecordingFile> implements MocapRecordingFile
{
	public static @Nullable RecordingFile get(CommandOutput out, String name)
	{
		return Files.check(out, name)
				? new RecordingFile(name, new File(Files.recordingsDirectory, name + Files.RECORDING_EXTENSION))
				: null;
	}

	private RecordingFile(String name, File file)
	{
		super(name, file);
	}

	@Override protected String getTextComponentKey(String key)
	{
		return "recordings." + key;
	}

	@Override public @Nullable MocapPlaybackRoot startPlayback(CommandInfo info, MocapModifiers modifiers, MocapPlaybackConfig config, boolean isHidden)
	{
		PlaybackDataManager dataManager = new PlaybackDataManager();
		if (!dataManager.loadRecording(info, this)) { return null; }

		RecordingPlayback playback = RecordingPlayback.start(info, true, dataManager, dataManager.getRecording(this), config, modifiers, null);
		return playback != null ? PlaybackManager.onStart(this, playback, isHidden) : null;
	}

	@Override public @Nullable Playback startAsSubscene(CommandInfo info, MocapModifiers modifiers, MocapPlaybackConfig config,
														PlaybackDataManager dataManager, PositionTransformer parentTransformer)
	{
		return RecordingPlayback.start(info, false, dataManager, dataManager.getRecording(this), config, modifiers, parentTransformer);
	}

	@Override public @Nullable Info getInfo(CommandOutput out)
	{
		return Info.load(out, this);
	}

	public record Info(
			int version,
			boolean experimental,
			int experimentalSubversion,
			long lengthInTicks,
			long sizeInBytes,
			long sizeInActionCount,
			Vec3 startPos,
			@Nullable Identifier assignedDimensionId,
			AssignedProfile assignedProfile,
			boolean legacyEndsWithDeath) implements MocapRecordingFile.Info
	{
		public static @Nullable Info load(CommandOutput out, @Nullable RecordingFile file)
		{
			RecordingData recording = new RecordingData();
			if (!recording.load(out, file) && recording.version <= RecordingFiles.VERSION)
			{
				out.sendFailure("recordings.info.failed");
				return null;
			}

			return new Info(
					recording.version,
					recording.experimentalVersion,
					recording.experimentalSubversion,
					recording.tickCount,
					recording.fileSize,
					recording.actions.size(),
					recording.startPos,
					recording.dimensionId,
					recording.assignedProfile,
					recording.endsWithDeath);
		}
	}
}
