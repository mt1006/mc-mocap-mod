package net.mt1006.mocap.mocap.playing.modifiers;

import com.mojang.authlib.properties.Property;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.api.v1.modifiers.MocapPlayerSkin;
import net.mt1006.mocap.mocap.files.SceneFiles;
import net.mt1006.mocap.mocap.playing.skins.CustomServerSkinManager;
import net.mt1006.mocap.mocap.settings.Settings;
import net.mt1006.mocap.utils.ProfileUtils;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class PlayerSkin implements MocapPlayerSkin
{
	public static final PlayerSkin DEFAULT = new PlayerSkin(Source.DEFAULT, null);
	private static final String MINESKIN_URL_PREFIX1 = "minesk.in/";
	private static final String MINESKIN_URL_PREFIX2 = "mineskin.org/skins/";
	private static final String MINESKIN_API_URL = "https://api.mineskin.org/get/uuid/";
	private final Source source;
	private final @Nullable String path;

	public PlayerSkin(Source source, @Nullable String path)
	{
		this.source = source;
		this.path = path;
	}

	public static PlayerSkin fromObject(@Nullable SceneFiles.Reader reader)
	{
		return reader != null
				? new PlayerSkin(reader.readEnum("skin_source", Source.DEFAULT), reader.readString("skin_path"))
				: DEFAULT;
	}

	public static @Nullable PlayerSkin createVerified(CommandOutput out, Source source, @Nullable String skinPath)
	{
		if (source == Source.FROM_MINESKIN && skinPath != null && !verifyMineskinUrl(skinPath))
		{
			out.sendFailure("failure.improper_mineskin_link");
			return null;
		}
		return new PlayerSkin(source, skinPath);
	}

	private static boolean verifyMineskinUrl(String url)
	{
		if (url.startsWith("https://")) { url = url.substring(8); }
		else if (url.startsWith("http://")) { url = url.substring(7); }

		return url.startsWith(MINESKIN_URL_PREFIX1) || url.startsWith(MINESKIN_URL_PREFIX2);
	}

	@Override public Source getSource()
	{
		return source;
	}

	@Override public @Nullable String getPath()
	{
		return path;
	}

	@Override public @Nullable SceneFiles.Writer save()
	{
		if (source == Source.DEFAULT) { return null; }

		SceneFiles.Writer writer = new SceneFiles.Writer();
		writer.addEnum("skin_source", source, Source.DEFAULT);
		writer.addString("skin_path", path);

		return writer;
	}

	@Override public @Nullable Property getSkinProperty(CommandInfo info, @Nullable Property oldProperty)
	{
		if (path == null) { return oldProperty; }

		switch (source)
		{
			case FROM_PLAYER:
				ProfileUtils.Profile profile = ProfileUtils.getProfile(info.getServer(), path, true);

				if (profile.skin == null)
				{
					info.sendFailure("playback.start.warning.skin.profile");
					return oldProperty;
				}
				return profile.skin;

			case FROM_FILE:
				return oldProperty; // handled by getCustomSkinProperty()

			case FROM_MINESKIN:
				if (!Settings.ALLOW_MINESKIN_REQUESTS.val) { return oldProperty; }
				Property skinProperty = propertyFromMineskinURL(path);

				if (skinProperty == null)
				{
					info.sendFailure("playback.start.warning.skin.mineskin");
					return oldProperty;
				}
				return skinProperty;

			default:
				return oldProperty;
		}
	}

	@Override public @Nullable Property getCustomSkinProperty()
	{
		return source == Source.FROM_FILE ? new Property(CustomServerSkinManager.PROPERTY_ID, path) : null;
	}

	@Override public MocapPlayerSkin mergeWithParent(MocapPlayerSkin parent)
	{
		return (source != Source.DEFAULT)
				? new PlayerSkin(source, path)
				: new PlayerSkin(parent.getSource(), parent.getPath());
	}

	private @Nullable Property propertyFromMineskinURL(String mineskinURL)
	{
		String mineskinID = mineskinURL.contains("/") ? mineskinURL.substring(mineskinURL.lastIndexOf('/') + 1) : mineskinURL;
		String mineskinApiURL = MINESKIN_API_URL + mineskinID;

		try
		{
			URL url = new URI(mineskinApiURL).toURL();

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
		catch (Exception e) { return null; }
	}
}
