package net.mt1006.mocap.api.v1.modifiers;

import com.mojang.authlib.properties.PropertyMap;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.mocap.files.SceneFiles;
import net.mt1006.mocap.mocap.playing.modifiers.PlayerSkin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface MocapPlayerSkin
{
	MocapPlayerSkin DEFAULT = new PlayerSkin(Source.DEFAULT, null);

	static MocapPlayerSkin fromPlayer(String path)
	{
		return new PlayerSkin(Source.FROM_PLAYER, path);
	}

	static MocapPlayerSkin fromFile(String path)
	{
		return new PlayerSkin(Source.FROM_FILE, path);
	}

	static MocapPlayerSkin fromMineskin(String path)
	{
		return new PlayerSkin(Source.FROM_MINESKIN, path);
	}

	Source getSource();

	@Nullable String getPath();

	@ApiStatus.Internal
	@Nullable SceneFiles.Writer save();

	@ApiStatus.Internal
	void addSkinToPropertyMap(CommandInfo info, PropertyMap propertyMap) throws IllegalArgumentException, IllegalAccessException;

	@ApiStatus.Internal
	MocapPlayerSkin mergeWithParent(MocapPlayerSkin parent);

	enum Source
	{
		DEFAULT,
		FROM_PLAYER,
		FROM_FILE,
		FROM_MINESKIN
	}
}
