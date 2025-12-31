package net.mt1006.mocap.mocap.playing.skins;

import com.google.gson.JsonObject;
import com.mojang.authlib.properties.Property;
import net.minecraft.util.GsonHelper;
import net.mt1006.mocap.utils.Cache;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

public class MineSkinSkins
{
	private static final String MINESKIN_URL_PREFIX1 = "minesk.in/";
	private static final String MINESKIN_URL_PREFIX2 = "mineskin.org/skins/";
	private static final String MINESKIN_API_URL = "https://api.mineskin.org/get/uuid/";
	private static final Pattern MINESKIN_UUID_PATTERN = Pattern.compile("[0-9a-f]*");
	private static final Cache<String, Property> cache = new Cache<>("MineSkin skins", 5);

	public static boolean verifyUrl(String url)
	{
		if (cache.containsKey(url)) { return true; }

		if (url.startsWith("https://")) { url = url.substring(8); }
		else if (url.startsWith("http://")) { url = url.substring(7); }

		if (url.startsWith(MINESKIN_URL_PREFIX1)) { url = url.substring(MINESKIN_URL_PREFIX1.length()); }
		else if (url.startsWith(MINESKIN_URL_PREFIX2)) { url = url.substring(MINESKIN_URL_PREFIX2.length()); }
		else { return false; }

		return MINESKIN_UUID_PATTERN.matcher(url).matches() && url.length() == 32;
	}

	public static @Nullable Property getProperty(String urlStr)
	{
		Property cachedProperty = cache.get(urlStr);
		if (cachedProperty != null) { return cachedProperty; }

		if (!verifyUrl(urlStr)) { return null; }
		String mineskinID = urlStr.contains("/") ? urlStr.substring(urlStr.lastIndexOf('/') + 1) : urlStr;
		String mineskinApiURL = MINESKIN_API_URL + mineskinID;

		try
		{
			URL url = new URI(mineskinApiURL).toURL();

			URLConnection connection = url.openConnection();
			if (!(connection instanceof HttpsURLConnection httpsConnection)) { return null; }

			httpsConnection.setUseCaches(false);
			httpsConnection.setRequestMethod("GET");

			InputStream stream = httpsConnection.getInputStream();
			String jsonStr = new String(stream.readAllBytes());
			stream.close();

			httpsConnection.disconnect();

			JsonObject json = GsonHelper.parse(jsonStr)
					.get("data").getAsJsonObject()
					.get("texture").getAsJsonObject();
			Property property = new Property("textures",
					json.get("value").getAsString(),
					json.get("signature").getAsString());

			cache.put(urlStr, property);
			return property;
		}
		catch (Exception e) { return null; }
	}

	public static void clearCache()
	{
		cache.clear();
	}
}
