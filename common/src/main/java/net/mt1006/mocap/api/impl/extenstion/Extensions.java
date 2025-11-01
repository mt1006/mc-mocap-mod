package net.mt1006.mocap.api.impl.extenstion;

import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.v1.extension.MocapExtension;
import net.mt1006.mocap.mocap.settings.Settings;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Extensions
{
	private static final int MAX_EXTENSION_COUNT = 256;
	private static final Map<String, MocapExtensionImpl> extensions = new HashMap<>();
	public static final byte FLAGS_IS_REQUIRED = 0b00000001;

	public static @Nullable MocapExtension registerExtension(String id, short version, boolean required)
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

		MocapExtensionImpl extension = new MocapExtensionImpl(id, version, required);
		extensions.put(extension.getId(), extension);
		return extension;
	}

	public static @Nullable MocapExtension getExtension(String id, short minVersion)
	{
		MocapExtension extension = extensions.get(id);
		return (extension == null || Math.abs(extension.getVersion()) < minVersion) ? null : extension;
	}

	public static Collection<MocapExtensionImpl> getExtensions()
	{
		return extensions.values();
	}

	public static boolean isRequired(boolean isRequiredFlag)
	{
		return switch (Settings.REQUIRED_EXTENSIONS.val)
		{
			case REQUIRE_ALL -> true;
			case IGNORE_ALL -> false;
			case LET_EXTENSION_DECIDE -> isRequiredFlag;
		};
	}
}
