package net.mt1006.mocap.api.impl.extenstion;

import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.v1.extension.MocapExtension;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Extensions
{
	private static final int MAX_EXTENSION_COUNT = 256;
	private static final Map<String, MocapExtensionImpl> extensions = new HashMap<>();

	public static @Nullable MocapExtension registerExtension(String id, short version)
	{
		if (extensions.containsKey(id))
		{
			MocapMod.LOGGER.warn("Trying to register extension with specific id multiple times - \"{}\"!", id);
			return null;
		}

		if (extensions.size() == MAX_EXTENSION_COUNT)
		{
			MocapMod.LOGGER.warn("Extension count limit reached! Rejecting extension - \"{}\"", id);
			return null;
		}

		MocapExtensionImpl extension = new MocapExtensionImpl(id, version);
		extensions.put(extension.getId(), extension);
		return extension;
	}

	public static @Nullable MocapExtension getExtension(String id, short minVersion)
	{
		MocapExtension extension = extensions.get(id);
		return (extension == null || Math.abs(extension.getVersion()) < minVersion) ? null : extension;
	}
}
