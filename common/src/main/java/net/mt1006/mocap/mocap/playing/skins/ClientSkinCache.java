package net.mt1006.mocap.mocap.playing.skins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSkinCache
{
	public static final ClientAsset.ResourceTexture WAITING_FOR_SKIN = createDummySkin();
	private static final long MAX_CUMULATIVE_TEXTURE_SIZE = 384L * (1 << 20);
	private static final int MAX_ELEMENT_COUNT = 4096;
	private final Map<String, ClientAsset.ResourceTexture> textureByName = new ConcurrentHashMap<>();
	private long sizeSum = 0L;
	private boolean playerWarned = false;

	public @Nullable ClientAsset.ResourceTexture get(String name)
	{
		return textureByName.get(name);
	}

	public void setWaitingForSkin(String name)
	{
		if (textureByName.size() >= MAX_ELEMENT_COUNT)
		{
			limitWarning(0L);
			return;
		}
		textureByName.put(name, WAITING_FOR_SKIN);
	}

	public void setLoaded(String name, int w, int h)
	{
		long size = 4L * w * h;
		if (sizeSum + size > MAX_CUMULATIVE_TEXTURE_SIZE || textureByName.size() + 1 > MAX_ELEMENT_COUNT)
		{
			limitWarning(size);
			return;
		}

		ResourceLocation id = CustomClientSkinManager.idFromName(name);
		textureByName.put(name, new ClientAsset.ResourceTexture(id, id));
		sizeSum += size;
	}

	private void limitWarning(long addedSize)
	{
		MocapMod.LOGGER.warn("Client skin cache reached its limits (s={}/{} B) (n={}/{}) - rejecting put!",
				sizeSum + addedSize, MAX_CUMULATIVE_TEXTURE_SIZE, textureByName.size() + 1, MAX_ELEMENT_COUNT);

		if (!playerWarned)
		{
			playerWarned = true; // or at least attempt was made

			Player player = Minecraft.getInstance().player;
			if (player == null) { return; }

			Utils.sendMessage(player, "warning.custom_skin_cache_limit");
			Utils.sendMessage(player, "warning.custom_skin_cache_limit.tip");
		}
	}

	public void clear()
	{
		TextureManager textureManager = Minecraft.getInstance().getTextureManager();
		for (Map.Entry<String, ClientAsset.ResourceTexture> entry : textureByName.entrySet())
		{
			ClientAsset.ResourceTexture val = entry.getValue();
			if (val != null && val != WAITING_FOR_SKIN)
			{
				ResourceLocation id = CustomClientSkinManager.idFromName(entry.getKey());
				textureManager.release(id);
			}
		}

		textureByName.clear();
		sizeSum = 0L;
		playerWarned = false;
	}

	private static ClientAsset.ResourceTexture createDummySkin()
	{
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MocapMod.MOD_ID, "dummy_skin/waiting_for_skin");
		return new ClientAsset.ResourceTexture(id, id);
	}
}
