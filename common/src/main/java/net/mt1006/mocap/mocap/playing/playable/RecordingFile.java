package net.mt1006.mocap.mocap.playing.playable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.controller.MocapPlaybackRoot;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.controller.playable.MocapRecordingFile;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.command.CommandSuggestions;
import net.mt1006.mocap.mocap.files.Files;
import net.mt1006.mocap.mocap.files.RecordingData;
import net.mt1006.mocap.mocap.files.RecordingFiles;
import net.mt1006.mocap.mocap.playing.PlaybackDataManager;
import net.mt1006.mocap.mocap.playing.PlaybackManager;
import net.mt1006.mocap.mocap.playing.playback.Playback;
import net.mt1006.mocap.mocap.playing.playback.PositionTransformer;
import net.mt1006.mocap.mocap.playing.playback.RecordingPlayback;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class RecordingFile extends Playable implements MocapRecordingFile
{
	private final String name;
	private final File file;

	private RecordingFile(String name, File file)
	{
		this.name = name;
		this.file = file;
	}

	public static @Nullable RecordingFile get(CommandOutput out, String name)
	{
		return Files.check(out, name)
				? new RecordingFile(name, new File(Files.recordingsDirectory, name + Files.RECORDING_EXTENSION))
				: null;
	}

	@Override public @Nullable MocapRecordingFile copy(CommandOutput out, MocapRecordingFile destFile)
	{
		try
		{
			FileUtils.copyFile(file, destFile.getFile());
		}
		catch (IOException e)
		{
			out.sendException(e, "recordings.copy.failed");
			return null;
		}

		CommandSuggestions.inputSet.add(destFile.getName());
		out.sendSuccess("recordings.copy.success");
		return destFile;
	}

	@Override public @Nullable MocapRecordingFile rename(CommandOutput out, MocapRecordingFile destFile)
	{
		if (!file.renameTo(destFile.getFile()))
		{
			out.sendFailure("recordings.rename.failed");
			return null;
		}

		CommandSuggestions.inputSet.remove(name);
		CommandSuggestions.inputSet.add(destFile.getName());
		out.sendSuccess("recordings.rename.success");
		return destFile;
	}

	@Override public boolean remove(CommandOutput out)
	{
		if (!file.delete()) { return out.sendFailure("recordings.remove.failed"); }

		CommandSuggestions.inputSet.remove(name);
		return out.sendSuccess("recordings.remove.success");
	}

	@Override public File getFile()
	{
		return file;
	}

	@Override public String getName()
	{
		return name;
	}

	@Override public boolean exists()
	{
		return file.exists();
	}

	@Override public @Nullable MocapPlaybackRoot startPlayback(CommandInfo info, MocapModifiers modifiers, MocapPlaybackConfig config, boolean isHidden)
	{
		PlaybackDataManager dataManager = new PlaybackDataManager();
		if (!dataManager.loadRecording(info, this)) { return null; }

		RecordingPlayback playback = RecordingPlayback.start(info, true, dataManager.getRecording(this), config, modifiers, null);
		return playback != null ? PlaybackManager.addPlayback(this, playback, isHidden) : null;
	}

	@Override public @Nullable Playback startAsSubscene(CommandInfo info, MocapModifiers modifiers, MocapPlaybackConfig config,
														PlaybackDataManager dataManager, PositionTransformer parentTransformer)
	{
		return RecordingPlayback.start(info, false, dataManager.getRecording(this), config, modifiers, parentTransformer);
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
			long sizeInOps,
			Vec3 startPos,
			@Nullable ResourceLocation assignedDimensionId,
			@Nullable String assignedPlayerName,
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
					recording.playerName,
					recording.endsWithDeath);
		}
	}
}
