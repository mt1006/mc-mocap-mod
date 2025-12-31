package net.mt1006.mocap.mocap.playing.playable;

import net.mt1006.mocap.api.v1.controller.MocapFile;
import net.mt1006.mocap.api.v1.controller.playable.MocapPlayable;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.command.CommandSuggestions;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public abstract class PlayableFile<T extends MocapPlayable & MocapFile<T>> extends Playable implements MocapFile<T>
{
	protected final String name;
	protected final File file;

	protected PlayableFile(String name, File file)
	{
		this.name = name;
		this.file = file;
	}

	@Override public @Nullable T copy(CommandOutput out, T destFile)
	{
		if (destFile.exists())
		{
			out.sendFailure(getTextComponentKey("failure.already_exists"));
			return null;
		}

		try
		{
			java.nio.file.Files.copy(getPath(), destFile.getPath());
		}
		catch (IOException e)
		{
			out.sendException(e, getTextComponentKey("copy.failed"));
			return null;
		}

		CommandSuggestions.inputSet.add(destFile.getName());
		out.sendSuccess(getTextComponentKey("copy.success"));
		return destFile;
	}

	@Override public @Nullable T rename(CommandOutput out, T destFile)
	{
		if (destFile.exists())
		{
			out.sendFailure(getTextComponentKey("failure.already_exists"));
			return null;
		}

		try
		{
			java.nio.file.Files.move(getPath(), destFile.getPath());
		}
		catch (IOException e)
		{
			out.sendFailure(getTextComponentKey("rename.failed"));
			return null;
		}

		CommandSuggestions.inputSet.remove(name);
		CommandSuggestions.inputSet.add(destFile.getName());
		out.sendSuccess(getTextComponentKey("rename.success"));
		return destFile;
	}

	@Override public boolean remove(CommandOutput out)
	{
		if (!file.delete()) { return out.sendFailure(getTextComponentKey("remove.failed")); }

		CommandSuggestions.inputSet.remove(name);
		return out.sendSuccess(getTextComponentKey("remove.success"));
	}

	@Override public File getFile()
	{
		return file;
	}

	@Override public Path getPath()
	{
		return file.toPath();
	}

	@Override public String getName()
	{
		return name;
	}

	@Override public boolean exists()
	{
		return file.exists();
	}

	protected abstract String getTextComponentKey(String key);
}
