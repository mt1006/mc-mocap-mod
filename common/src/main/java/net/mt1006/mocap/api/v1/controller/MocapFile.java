package net.mt1006.mocap.api.v1.controller;

import net.mt1006.mocap.api.v1.io.CommandOutput;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface MocapFile<T extends MocapFile<T>>
{
	boolean exists();

	@Nullable T copy(CommandOutput out, T destFile);

	@Nullable T rename(CommandOutput out, T destFile);

	boolean remove(CommandOutput out);

	File getFile();
}
