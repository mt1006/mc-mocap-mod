package com.mt1006.mocap.mocap.playing;

import com.mt1006.mocap.mocap.files.Files;
import com.mt1006.mocap.network.MocapPacketS2C;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CustomSkinManager
{
	public static final String PROPERTY_ID = "mocap:skin_from_file";
	private static final ConcurrentMap<String, byte[]> serverMap = new ConcurrentHashMap<>();

	public static void sendSkinToClient(ServerPlayer player, String name)
	{
		byte[] image = serverMap.get(name);
		if (image != null) { MocapPacketS2C.sendCustomSkinData(player, name, image); }
		else { Util.backgroundExecutor().execute(() -> sendSkinToClientThread(player, name)); }
	}

	public static void sendSkinToClientThread(ServerPlayer player, String name)
	{
		if (!checkIfProperName(null, name)) { return; }
		byte[] array = Files.loadFile(Files.getSkinFile(player.getServer(), name));

		if (array != null)
		{
			serverMap.put(name, array);
			MocapPacketS2C.sendCustomSkinData(player, name, array);
		}
	}

	public static boolean checkIfProperName(@Nullable CommandSourceStack commandSource, String name)
	{
		return Files.checkIfProperName(commandSource, name.startsWith("slim/") ? name.substring(5) : name);
	}
}
