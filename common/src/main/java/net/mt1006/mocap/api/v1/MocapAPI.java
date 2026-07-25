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
	/**
	 * Executes given runnable right after Motion Capture is initialized, or now if it's already initialized.
	 * @param runnable function to execute
	 */
	public static void executeAfterInit(Runnable runnable)
	{
		if (MocapMod.initialized) { runnable.run(); }
		else { MocapMod.toRunOnInit.add(runnable); }
	}

	/**
	 * Creates "controller" for using Motion Capture on a similar level as in-game commands.
	 * Controller should be created each time server is loaded, and destroyed (reference set to null)
	 * each time server is stopping. For this use SERVER_START_POST and SERVER_STOP_POST mocap events.
	 * As of current version there might be multiple controllers with the same name, but it might
	 * change in the future, so it's recommended to give unique ones.
	 * @param server server to be associated with controller
	 * @param name name of a controller (e.g. mod ID), it should follow mocap file naming rules
	 * @return controller instance, or null if something went wrong
	 * @see net.mt1006.mocap.api.v1.events.MocapEvents#SERVER_START_POST
	 * @see net.mt1006.mocap.api.v1.events.MocapEvents#SERVER_STOP_POST
	 */
	public static @Nullable MocapController createController(MinecraftServer server, String name)
	{
		if (!MocapMod.initializedExceptApi)
		{
			MocapMod.LOGGER.warn("Registering controller {} before mocap was initialized! Use executeAfterInit()", name);
		}

		if (!Files.checkIfProperName(CommandOutput.DUMMY, name))
		{
			MocapMod.LOGGER.warn("Failed to create MocapController - improper name!");
			return null;
		}
		return new MocapControllerImpl(server, name);
	}

	//TODO: remove "required"?
	/**
	 * Shorter version of 3-argument registerExtension(), but with "required" set to false.
	 * @see MocapAPI#registerExtension(String, short, boolean)
	 */
	public static @Nullable MocapExtension registerExtension(String id, short version)
	{
		return registerExtension(id, version, false);
	}

	/**
	 * Registers "extension" with a given ID. Extensions can be used to add custom action types to recordings.
	 * When introducing changes to extension, they should be backwards compatible.
	 * If changes break forward compatibility, version number should be increased.
	 * If changes break backward compatibility, new id should be used, e.g. "mymod" -> "mymod2",
	 * but as already mentioned, you should avoid such changes.
	 * @param id unique identifier of an extension (e.g. mod ID), it should follow mocap file naming rules
	 * @param version version of an extension
	 * @param required whenever playing back recording recorded with extension installed,
	 *                 also requires extension to be installed
	 * @return extension instance, or null if something went wrong
	 */
	public static @Nullable MocapExtension registerExtension(String id, short version, boolean required)
	{
		if (!MocapMod.initializedExceptApi)
		{
			MocapMod.LOGGER.warn("Registering extension {} before mocap was initialized! Use executeAfterInit()", id);
		}

		if (!Files.checkIfProperName(CommandOutput.DUMMY, id))
		{
			MocapMod.LOGGER.warn("Failed to create MocapExtension - improper id!");
			return null;
		}
		return Extensions.registerExtension(id, version, required);
	}

	/**
	 * Class for retrieving various information about Motion Capture version.
	 */
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
			String fullVersion = MocapMod.loaderInterface.getModVersion();
			int plusPos = fullVersion.indexOf("+mc");
			return plusPos != -1 ? fullVersion.substring(0, plusPos) : "0.0";
		}

		public static String forVersion()
		{
			String fullVersion = MocapMod.loaderInterface.getModVersion();
			int plusPos = fullVersion.indexOf("+mc");
			return (plusPos != -1 && fullVersion.length() > plusPos + 2) ? fullVersion.substring(plusPos + 3) : "0.0";
		}

		public static String fullVersion()
		{
			return MocapMod.loaderInterface.getModVersion();
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
