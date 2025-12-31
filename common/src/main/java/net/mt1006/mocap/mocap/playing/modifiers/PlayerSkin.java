package net.mt1006.mocap.mocap.playing.modifiers;

import com.mojang.authlib.properties.Property;
import net.mt1006.mocap.api.v1.controller.config.MocapPlayerNameHandling;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.api.v1.modifiers.MocapPlayerSkin;
import net.mt1006.mocap.mocap.files.Files;
import net.mt1006.mocap.mocap.files.SceneFiles;
import net.mt1006.mocap.mocap.files.SkinList;
import net.mt1006.mocap.mocap.playing.PlaybackDataManager;
import net.mt1006.mocap.mocap.playing.skins.CustomServerSkinManager;
import net.mt1006.mocap.mocap.playing.skins.MineSkinSkins;
import net.mt1006.mocap.mocap.settings.Settings;
import net.mt1006.mocap.utils.ProfileUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

public class PlayerSkin implements MocapPlayerSkin
{
	public static final PlayerSkin DEFAULT = new PlayerSkin(Source.DEFAULT, null);
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
		if (source == Source.FROM_MINESKIN && skinPath != null && !MineSkinSkins.verifyUrl(skinPath))
		{
			out.sendFailure("failure.improper_mineskin_link");
			return null;
		}
		return new PlayerSkin(source, skinPath);
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
				ProfileUtils.Profile profile = ProfileUtils.getProfile(info.getServer(),
						MocapPlayerNameHandling.IGNORE_AND_REPLACE_CASING, path, true);

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
				Property skinProperty = MineSkinSkins.getProperty(path);

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

	@Override public MocapPlayerSkin resolveList(PlaybackDataManager dataManager)
	{
		if (!isSkinList()) { return this; }

		PlayerSkin skin = this;
		Stack<String> listStack = new Stack<>();

		while (skin.isSkinList())
		{
			if (listStack.contains(skin.path)) { return this; } // error - loop of lists
			SkinList skinList = dataManager.loadOrGetSkinList(skin.path);
			listStack.add(skin.path);

			if (skinList == null) { return this; } // error - failed to load skin list
			skin = skinList.getRandomSkin(dataManager.random);
		}
		return skin;
	}

	private boolean isSkinList()
	{
		return source == Source.FROM_FILE && path != null && path.startsWith(Files.SKIN_LIST_PREFIX);
	}
}
