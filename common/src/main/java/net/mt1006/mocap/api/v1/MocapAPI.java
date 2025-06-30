package net.mt1006.mocap.api.v1;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.impl.controller.MocapControllerImpl;
import net.mt1006.mocap.api.impl.extenstion.Extensions;
import net.mt1006.mocap.api.v1.controller.MocapController;
import net.mt1006.mocap.api.v1.extension.MocapExtension;
import net.mt1006.mocap.command.io.CommandOutput;
import net.mt1006.mocap.mocap.files.Files;
import org.jetbrains.annotations.Nullable;

public final class MocapAPI
{
	public static @Nullable MocapController createController(String name, ServerLevel level)
	{
		return createController(name, level, true);
	}

	public static @Nullable MocapController createController(String name, ServerLevel level, boolean hideStuff)
	{
		if (Files.checkIfProperName(CommandOutput.DUMMY, name))
		{
			MocapMod.LOGGER.warn("Failed to create MocapController - improper name!");
			return null;
		}
		return new MocapControllerImpl(name, level, hideStuff);
	}

	public static @Nullable MocapExtension registerExtension(MinecraftServer server, String id, short version)
	{
		if (!server.isSameThread()) { throw new RuntimeException("Trying to register mocap extension on non-server thread!"); }
		return Extensions.registerExtension(id, version);
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

		public static int sceneFormatVersion()
		{
			return MocapMod.SCENE_FORMAT_VERSION;
		}
	}
}
