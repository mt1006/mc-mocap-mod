package net.mt1006.mocap.api.v1;

import net.minecraft.server.MinecraftServer;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.impl.controller.MocapControllerImpl;
import net.mt1006.mocap.api.impl.extenstion.Extensions;
import net.mt1006.mocap.api.v1.controller.MocapController;
import net.mt1006.mocap.api.v1.extension.MocapExtension;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.mocap.files.Files;
import org.jetbrains.annotations.Nullable;

public final class MocapAPI
{
	public static @Nullable MocapController createController(MinecraftServer server, String name)
	{
		if (Files.checkIfProperName(CommandOutput.DUMMY, name))
		{
			MocapMod.LOGGER.warn("Failed to create MocapController - improper name!");
			return null;
		}
		return new MocapControllerImpl(server, name);
	}

	public static @Nullable MocapExtension registerExtension(String id, short version)
	{
		return Extensions.registerExtension(id, version, false);
	}

	public static @Nullable MocapExtension registerExtension(String id, short version, boolean required)
	{
		return Extensions.registerExtension(id, version, required);
	}

	public static void executeAfterInit(Runnable runnable)
	{
		if (MocapMod.initialized) { runnable.run(); }
		else { MocapMod.toRunOnInit.add(runnable); }
	}

	public static class Info
	{
		public static String name()
		{
			return MocapMod.getName();
		}

		public static String fullName()
		{
			return MocapMod.getFullName();
		}

		public static String version()
		{
			return MocapMod.loaderInterface.getModVersion();
		}

		public static String forVersion()
		{
			return MocapMod.FOR_VERSION;
		}

		public static String forLoader()
		{
			return MocapMod.loaderInterface.getLoaderName();
		}

		public static boolean isExperimental()
		{
			return MocapMod.EXPERIMENTAL;
		}

		public static int recordingFormatVersion()
		{
			return MocapMod.RECORDING_FORMAT_VERSION;
		}

		public static int recordingFormatExpSubversion()
		{
			return MocapMod.RECORDING_FORMAT_EXP_SUBVERSION;
		}

		public static int sceneFormatVersion()
		{
			return MocapMod.SCENE_FORMAT_VERSION;
		}
	}
}
