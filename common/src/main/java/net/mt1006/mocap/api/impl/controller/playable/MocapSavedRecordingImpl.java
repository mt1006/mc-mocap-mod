package net.mt1006.mocap.api.impl.controller.playable;

import net.mt1006.mocap.api.impl.controller.MocapControllerImpl;
import net.mt1006.mocap.api.v1.controller.playable.MocapSavedRecording;
import net.mt1006.mocap.mocap.files.Files;
import net.mt1006.mocap.mocap.files.RecordingFiles;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class MocapSavedRecordingImpl extends MocapPlayableImpl implements MocapSavedRecording
{
	private String name;

	public MocapSavedRecordingImpl(MocapControllerImpl ctrl, String name)
	{
		super(ctrl);
		this.name = name;
	}

	@Override public String getId()
	{
		return name;
	}

	@Override public boolean exists()
	{
		return getFile() != null;
	}

	@Override public boolean remove()
	{
		return RecordingFiles.remove(ctrl.commandInfo, name);
	}

	@Override public boolean rename(String name)
	{
		boolean success = RecordingFiles.rename(ctrl.commandInfo, this.name, name);
		if (success) { this.name = name; }
		return success;
	}

	@Override public @Nullable MocapSavedRecording copy(String name)
	{
		boolean success = RecordingFiles.copy(ctrl.commandInfo, this.name, name);
		return success ? new MocapSavedRecordingImpl(ctrl, name) : null;
	}

	@Override public @Nullable File getFile()
	{
		return Files.getRecordingFile(ctrl.commandInfo, name);
	}

	@Override public @Nullable Info getInfo()
	{
		return RecordingFiles.Info.load(ctrl.commandInfo, name);
	}
}
