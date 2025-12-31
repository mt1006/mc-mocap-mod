package net.mt1006.mocap.mocap.playing.skins;

import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.mocap.files.Files;
import net.mt1006.mocap.network.MocapPacketS2C;

public class CustomServerSkinManager
{
	public static final String PROPERTY_ID = "mocap:skin_from_file";
	private static final ServerSkinCache cache = new ServerSkinCache();

	public static void sendSkinToClient(ServerPlayer player, String name)
	{
		byte[] image = cache.get(name);
		if (image != null) { MocapPacketS2C.sendCustomSkinData(player, name, image); }
		else { Util.backgroundExecutor().execute(() -> sendSkinToClientThread(player, name)); }
	}

	public static void sendSkinToClientThread(ServerPlayer player, String name)
	{
		if (!checkIfProperName(CommandOutput.DUMMY, name)) { return; }

		byte[] array = Files.loadFile(Files.getSkinFile(name));
		if (array == null) { return; }

		if (array.length > CustomClientSkinManager.MAX_ACCEPTED_FILE_SIZE)
		{
			MocapMod.LOGGER.warn("Rejecting to send custom skin file - bigger than {} MiB!",
					CustomClientSkinManager.MAX_ACCEPTED_FILE_SIZE / (1 << 20));
			return;
		}

		cache.put(name, array);
		MocapPacketS2C.sendCustomSkinData(player, name, array);
	}

	public static boolean checkIfProperName(CommandOutput out, String name)
	{
		return Files.checkIfProperName(out, name.startsWith(Files.SLIM_SKIN_PREFIX)
				? name.substring(Files.SLIM_SKIN_PREFIX.length())
				: name);
	}

	public static void clearCache()
	{
		cache.clear();
	}
}
