package net.mt1006.mocap.api.v1.controller;

import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface MocapFile<T extends MocapFile<T>>
{
	boolean exists();

	boolean remove();

	boolean rename(String name);

	@Nullable T copy(String name);

	@Nullable File getFile();
}
