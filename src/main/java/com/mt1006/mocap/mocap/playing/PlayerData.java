package com.mt1006.mocap.mocap.playing;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mt1006.mocap.mocap.settings.Settings;
import com.mt1006.mocap.utils.Fields;
import com.mt1006.mocap.utils.ProfileUtils;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class PlayerData
{
	private static final String MINESKIN_API_URL = "https://api.mineskin.org/get/uuid/";
	public final @Nullable String name;
	public final SkinSource skinSource;
	public final String skinPath;

	public PlayerData(@Nullable String name)
	{
		this.name = name;
		this.skinSource = SkinSource.DEFAULT;
		this.skinPath = Utils.NULL_STR;
	}

	public PlayerData(@Nullable String name, SkinSource skinSource, @Nullable String skinPath)
	{
		this.name = name;
		this.skinSource = skinSource;
		this.skinPath = Utils.toNotNullStr(skinPath);
	}

	public PlayerData(Scanner scanner)
	{
		String name = null;
		SkinSource skinSource = SkinSource.DEFAULT;
		String skinPath = Utils.NULL_STR;

		try
		{
			String nameStr = scanner.next();
			name = nameStr.length() <= 16 ? Utils.toNullableStr(nameStr) : null;

			skinPath = scanner.next();

			// Pre-1.3 compatibility
			if (!skinPath.equals(Utils.NULL_STR)) { skinSource = SkinSource.FROM_MINESKIN; }

			skinSource = SkinSource.fromID(Integer.parseInt(scanner.next()));
		}
		catch (Exception ignore) {}

		this.name = name;
		this.skinSource = skinSource;
		this.skinPath = skinPath;
	}

	public String dataToStr()
	{
		return String.format("%s %s %d", Utils.toNotNullStr(name), skinPath, skinSource.id);
	}

	public void addSkinToPropertyMap(CommandSourceStack commandSource, PropertyMap propertyMap)
			throws IllegalArgumentException, IllegalAccessException
	{
		switch (skinSource)
		{
			case FROM_PLAYER:
				GameProfile tempProfile = ProfileUtils.getGameProfile(commandSource.getServer(), skinPath);
				PropertyMap tempPropertyMap = (PropertyMap)Fields.gameProfileProperties.get(tempProfile);

				if (!tempPropertyMap.containsKey("textures"))
				{
					Utils.sendFailure(commandSource, "mocap.playing.start.warning.skin.profile");
					break;
				}

				if (propertyMap.containsKey("textures")) { propertyMap.get("textures").clear(); }
				propertyMap.putAll("textures", tempPropertyMap.get("textures"));
				break;

			case FROM_FILE:
				propertyMap.put(CustomSkinManager.PROPERTY_ID, new Property(CustomSkinManager.PROPERTY_ID, skinPath));
				break;

			case FROM_MINESKIN:
				if (!Settings.ALLOW_MINESKIN_REQUESTS.val) { return; }
				Property skinProperty = propertyFromMineskinURL(skinPath);

				if (skinProperty == null)
				{
					Utils.sendFailure(commandSource, "mocap.playing.start.warning.skin.mineskin");
					break;
				}

				if (propertyMap.containsKey("textures")) { propertyMap.get("textures").clear(); }
				propertyMap.put("textures", skinProperty);
				break;
		}
	}

	public PlayerData mergeWithParent(PlayerData parent)
	{
		if (skinSource != SkinSource.DEFAULT) { return new PlayerData(name != null ? name : parent.name, skinSource, skinPath); }
		else  { return new PlayerData(name != null ? name : parent.name, parent.skinSource, parent.skinPath); }
	}

	private @Nullable Property propertyFromMineskinURL(String mineskinURL)
	{
		String mineskinID = mineskinURL.contains("/") ? mineskinURL.substring(mineskinURL.lastIndexOf('/') + 1) : mineskinURL;
		String mineskinApiURL = MINESKIN_API_URL + mineskinID;

		try
		{
			URL url = new URL(mineskinApiURL);

			//TODO: fallback to java 8 style
			URLConnection connection = url.openConnection();
			if (!(connection instanceof HttpsURLConnection httpsConnection)) { return null; }

			httpsConnection.setUseCaches(false);
			httpsConnection.setRequestMethod("GET");

			Scanner scanner = new Scanner(httpsConnection.getInputStream());
			String text = scanner.useDelimiter("\\A").next();

			scanner.close();
			httpsConnection.disconnect();

			String value = text.split("\"value\":\"")[1].split("\"")[0];
			String signature = text.split("\"signature\":\"")[1].split("\"")[0];

			return new Property("textures", value, signature);
		}
		catch (Exception exception) { return null; }
	}

	public enum SkinSource
	{
		DEFAULT(0),
		FROM_PLAYER(1),
		FROM_FILE(2),
		FROM_MINESKIN(3);

		public final int id;

		SkinSource(int id)
		{
			this.id = id;
		}

		public static SkinSource fromID(int id)
		{
			for (SkinSource s : values())
			{
				if (s.id == id) { return s; }
			}
			return DEFAULT;
		}
	}
}
