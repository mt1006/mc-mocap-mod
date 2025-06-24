package net.mt1006.mocap.api.v1.modifiers;

import net.mt1006.mocap.mocap.playing.modifiers.PlayerSkin;
import org.jetbrains.annotations.Nullable;

public interface MocapPlayerSkin
{
	MocapPlayerSkin DEFAULT = PlayerSkin.DEFAULT;

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

	enum Source
	{
		DEFAULT,
		FROM_PLAYER,
		FROM_FILE,
		FROM_MINESKIN;

		public static Source fromName(@Nullable String name)
		{
			if (name == null) { return DEFAULT; }

			try
			{
				return valueOf(name.toUpperCase());
			}
			catch (IllegalArgumentException e) { return DEFAULT; }
		}

		@Override public String toString()
		{
			return name().toLowerCase();
		}
	}
}
