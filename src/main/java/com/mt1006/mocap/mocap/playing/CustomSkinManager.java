package com.mt1006.mocap.mocap.playing;

import com.mt1006.mocap.command.CommandOutput;
import com.mt1006.mocap.mocap.files.Files;
import com.mt1006.mocap.network.MocapPacketS2C;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CustomSkinManager
{
	public static final String PROPERTY_ID = "mocap:skin_from_file";
	private static final ConcurrentMap<String, byte[]> serverMap = new ConcurrentHashMap<>();

	public static void sendSkinToClient(ServerPlayerEntity player, String name)
	{
		byte[] image = serverMap.get(name);
		if (image != null) { MocapPacketS2C.sendCustomSkinData(player, name, image); }
		else { Util.backgroundExecutor().execute(() -> sendSkinToClientThread(player, name)); }
	}

	public static void sendSkinToClientThread(ServerPlayerEntity player, String name)
	{
		if (!checkIfProperName(CommandOutput.DUMMY, name)) { return; }
		byte[] array = Files.loadFile(Files.getSkinFile(player.getServer(), name));

		if (array != null)
		{
			serverMap.put(name, array);
			MocapPacketS2C.sendCustomSkinData(player, name, array);
		}
	}

	public static boolean checkIfProperName(CommandOutput commandOutput, String name)
	{
		return Files.checkIfProperName(commandOutput, name.startsWith("slim/") ? name.substring(5) : name);
	}
}
