package net.mt1006.mocap.mocap.files;

import net.mt1006.mocap.api.v1.modifiers.MocapPlayerSkin;
import net.mt1006.mocap.mocap.playing.modifiers.PlayerSkin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class SkinList
{
	private static final PlayerSkin INVALID = new PlayerSkin(MocapPlayerSkin.Source.DEFAULT, null);
	private final List<PlayerSkin> skins;

	private SkinList(List<PlayerSkin> skins)
	{
		this.skins = List.copyOf(skins);
	}

	public static @Nullable SkinList load(File file)
	{
		try
		{
			List<String> lines = Files.readAllLines(file.toPath());
			List<PlayerSkin> skins = new ArrayList<>();

			for (String line : lines)
			{
				PlayerSkin skin = parse(line);
				if (skin == INVALID) { return null; }
				else if (skin != null) { skins.add(skin); }
			}

			return new SkinList(skins);
		}
		catch (IOException e) { return null; }
	}

	private static @Nullable PlayerSkin parse(String line)
	{
		if (line.isEmpty()) { return null; }

		String[] parts = line.split(" ");
		if (parts.length != 2) { return INVALID; }

		MocapPlayerSkin.Source skinSource;
		try
		{
			skinSource = MocapPlayerSkin.Source.valueOf(parts[0].toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException e) { return null; }

		return new PlayerSkin(skinSource, parts[1]);
	}

	public PlayerSkin getRandomSkin(Random rand)
	{
		return skins.get(rand.nextInt(skins.size()));
	}
}
