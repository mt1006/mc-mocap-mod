package net.mt1006.mocap.api.v1.controller;

import net.mt1006.mocap.api.v1.io.CommandOutput;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

public interface MocapFile<T extends MocapFile<T>>
{
	/**
	 * @return true if file exists, false otherwise
	 */
	boolean exists();

	/**
	 * Copies this file to destFile. If destFile already exists, copy fails.
	 * To create object for destFile use getPlayable() which always succeeds for valid filenames.
	 * @param out log output
	 * @param destFile reference to not yet existing file with a destination path
	 * @return destFile on success, null otherwise
	 * @see MocapController#getPlayable(String)
	 */
	@Nullable T copy(CommandOutput out, T destFile);

	/**
	 * Moves (renames) this file to destFile. If destFile already exists, renaming fails.
	 * To create object for destFile use getPlayable() which always succeeds for valid filenames.
	 * @param out log output
	 * @param destFile reference to not yet existing file with a destination path
	 * @return destFile on success, null otherwise
	 * @see MocapController#getPlayable(String)
	 */
	@Nullable T rename(CommandOutput out, T destFile);

	/**
	 * Removes this file.
	 * @param out log output
	 * @return true on success, false otherwise
	 */
	boolean remove(CommandOutput out);

	/**
	 * @return File object referenced by this object
	 */
	File getFile();

	/**
	 * @return absolute path to file referenced by this object
	 */
	Path getPath();
}
