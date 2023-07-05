package com.mt1006.mocap.mocap.playing;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.mocap.files.Files;
import com.mt1006.mocap.network.MocapPacketC2S;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CustomClientSkinManager
{
	private static final int MAX_CLIENT_CACHE_SIZE = 4096;
	private static final String SKIN_RES_PREFIX = "custom_skin/";
	private static final String SLIM_SKIN_RES_PREFIX = SKIN_RES_PREFIX + Files.SLIM_SKIN_PREFIX;
	private static final ConcurrentMap<String, Boolean> clientMap = new ConcurrentHashMap<>();
	private static boolean clientWarned = false;

	public static @Nullable ResourceLocation get(@Nullable String name)
	{
		if (name == null) { return null; }
		Boolean accessible = clientMap.get(name);

		if (accessible == null)
		{
			loadClientSkin(name);
			return null;
		}
		return accessible ? resFromName(name) : null;
	}

	public static void loadClientSkin(String name)
	{
		if (clientMap.size() > MAX_CLIENT_CACHE_SIZE)
		{
			if (clientWarned) { return; }

			Player player = Minecraft.getInstance().player;
			if (player == null) { return; }

			Utils.sendSystemMessage(player, "mocap.warning.custom_skin_cache_limit");
			Utils.sendSystemMessage(player, "mocap.warning.custom_skin_cache_limit.tip");
			clientWarned = true;
			return;
		}

		clientMap.put(name, false);
		MocapPacketC2S.sendRequestCustomSkin(name);
	}

	public static void register(Pair<String, byte[]> customSkinData)
	{
		String name = customSkinData.getFirst();
		byte[] array = customSkinData.getSecond();

		Boolean accessible = clientMap.get(name);
		if (accessible == null || accessible) { return; }

		try
		{
			Minecraft.getInstance().getTextureManager().register(resFromName(name), new DynamicTexture(NativeImage.read(array)));
			clientMap.put(name, true);
		}
		catch (IOException ignore) {}
	}

	public static void clearCache()
	{
		TextureManager textureManager = Minecraft.getInstance().getTextureManager();
		for (Map.Entry<String, Boolean> entry : clientMap.entrySet())
		{
			Boolean val = entry.getValue();
			if (val != null && val) { textureManager.release(resFromName(entry.getKey())); }
		}
		clientMap.clear();
		clientWarned = false;
	}

	public static boolean isSlimSkin(ResourceLocation res)
	{
		return res.getPath().startsWith(SLIM_SKIN_RES_PREFIX);
	}

	private static ResourceLocation resFromName(String name)
	{
		return new ResourceLocation(MocapMod.MOD_ID, SKIN_RES_PREFIX + name);
	}
}
