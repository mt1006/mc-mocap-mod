package net.mt1006.mocap;

import net.minecraft.server.MinecraftServer;
import net.mt1006.mocap.mocap.actions.ActionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MocapMod
{
	public static final String MOD_ID = "mocap";
	public static final String FOR_VERSION = "1.21.10";
	public static final boolean EXPERIMENTAL = true; //TODO: change it to false

	public static final byte RECORDING_FORMAT_VERSION = 5;
	public static final byte RECORDING_FORMAT_EXP_SUBVERSION = 1;
	public static final byte SCENE_FORMAT_VERSION = 4;
	public static final int NETWORK_PACKETS_VERSION = 5;

	public static final Logger LOGGER = LogManager.getLogger();
	public static final List<Runnable> toRunOnInit = new ArrayList<>();
	public static boolean initialized = false;
	public static boolean isDedicatedServer = false;
	public static MocapModLoaderInterface loaderInterface = null;
	public static @Nullable MinecraftServer server = null;

	public static void init(boolean isDedicatedServer, MocapModLoaderInterface loaderInterface)
	{
		MocapMod.isDedicatedServer = isDedicatedServer;
		MocapMod.loaderInterface = loaderInterface;

		ActionType.initTypes();
	}

	public static void postInit()
	{
		toRunOnInit.forEach(Runnable::run);
		toRunOnInit.clear();
		initialized = true;
	}

	public static String getName()
	{
		return String.format("Mocap v%s", loaderInterface.getModVersion());
	}

	public static String getFullName()
	{
		return String.format("Mocap v%s for Minecraft %s [%s]",
				loaderInterface.getModVersion(), FOR_VERSION, loaderInterface.getLoaderName());
	}
}
