package com.mt1006.mocap.mocap.playing;

import com.mojang.datafixers.util.Pair;
import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.mocap.files.Files;
import com.mt1006.mocap.network.MocapPacketC2S;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
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

			PlayerEntity player = Minecraft.getInstance().player;
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

		ByteBuffer byteBuffer = null;

		try
		{
			NativeImage nativeImage;

			byteBuffer = MemoryUtil.memAlloc(array.length);
			try
			{
				byteBuffer.put(array);
				byteBuffer.rewind();
				nativeImage = NativeImage.read(byteBuffer);
			}
			catch (Exception exception)
			{
				Utils.exception(exception, "Failed to load skin texture into buffer!");
				MemoryUtil.memFree(byteBuffer);
				return;
			}
			MemoryUtil.memFree(byteBuffer);
			byteBuffer = null;

			if (nativeImage.getWidth() > 4096 || nativeImage.getHeight() > 4096)
			{
				MocapMod.LOGGER.error("Skin texture too big!");
				return;
			}

			Minecraft.getInstance().getTextureManager().register(resFromName(name), new DynamicTexture(nativeImage));
			clientMap.put(name, true);
		}
		catch (Exception exception)
		{
			Utils.exception(exception, "Failed to read skin texture!");
			MemoryUtil.memFree(byteBuffer);
		}
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
