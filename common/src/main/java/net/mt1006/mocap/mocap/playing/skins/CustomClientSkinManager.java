package net.mt1006.mocap.mocap.playing.skins;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.mocap.files.Files;
import net.mt1006.mocap.network.MocapPacketC2S;
import net.mt1006.mocap.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class CustomClientSkinManager
{
	public static final int MAX_ACCEPTED_FILE_SIZE = 20 * (1 << 20);
	private static final String SKIN_RES_PREFIX = "custom_skin/";
	private static final String SLIM_SKIN_RES_PREFIX = SKIN_RES_PREFIX + Files.SLIM_SKIN_PREFIX;
	private static final ClientSkinCache cache = new ClientSkinCache();

	public static @Nullable ResourceLocation get(@Nullable String name)
	{
		if (name == null) { return null; }

		ResourceLocation skin = cache.get(name);
		if (skin == null)
		{
			cache.setWaitingForSkin(name);
			MocapPacketC2S.sendRequestCustomSkin(name);
			return null;
		}
		else if (skin == ClientSkinCache.WAITING_FOR_SKIN)
		{
			return null;
		}
		else
		{
			return skin;
		}
	}

	public static void register(Pair<String, byte[]> customSkinData)
	{
		String name = customSkinData.getFirst();
		byte[] array = customSkinData.getSecond();

		ResourceLocation skin = cache.get(name);
		if (skin != ClientSkinCache.WAITING_FOR_SKIN) { return; }

		if (array.length > MAX_ACCEPTED_FILE_SIZE)
		{
			MocapMod.LOGGER.warn("Rejecting to accept custom skin file - bigger than {} MiB!",
					MAX_ACCEPTED_FILE_SIZE / (1 << 20));
			return;
		}

		try
		{
			NativeImage nativeImage;
			try
			{
				nativeImage = NativeImage.read(array);
			}
			catch (IOException e)
			{
				Utils.exception(e, "Failed to load skin texture into buffer!");
				return;
			}

			if (nativeImage.getWidth() > 4096 || nativeImage.getHeight() > 4096)
			{
				MocapMod.LOGGER.warn("Custom skin texture too big!");
				return;
			}

			ResourceLocation id = idFromName(name);
			Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(id::toString, nativeImage));
			cache.setLoaded(name, nativeImage.getWidth(), nativeImage.getHeight());
		}
		catch (Exception e) { Utils.exception(e, "Failed to read skin texture!"); }
	}

	public static void clearCache()
	{
		cache.clear();
	}

	public static boolean isSlimSkin(ResourceLocation res)
	{
		return res.getPath().startsWith(SLIM_SKIN_RES_PREFIX);
	}

	public static ResourceLocation idFromName(String name)
	{
		return ResourceLocation.fromNamespaceAndPath(MocapMod.MOD_ID, SKIN_RES_PREFIX + name);
	}
}
