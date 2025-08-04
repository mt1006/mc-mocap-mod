package net.mt1006.mocap.mocap.files;

import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.api.v1.modifiers.MocapPlayerSkin;
import net.mt1006.mocap.api.v1.modifiers.MocapStartDelay;
import net.mt1006.mocap.mocap.playing.modifiers.PlaybackModifiers;
import net.mt1006.mocap.mocap.playing.modifiers.PlayerAsEntity;
import net.mt1006.mocap.mocap.playing.modifiers.PlayerSkin;
import net.mt1006.mocap.mocap.playing.modifiers.Transformations;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.util.Scanner;

public class LegacySceneDataParser
{
	private static final String NULL_STR = "[null]";
	private boolean legacy = true, parsed = false;

	public LegacySceneDataParser(SceneData sceneData, CommandOutput out, byte[] scene)
	{
		try (Scanner scanner = new Scanner(new ByteArrayInputStream(scene)))
		{
			int versionNumber;
			try
			{
				versionNumber = Integer.parseInt(scanner.next());
			}
			catch (NumberFormatException e)
			{
				legacy = false;
				return;
			}

			if (!sceneData.setAndVerifyVersion(out, versionNumber)) { scanner.close(); }
			if (scanner.hasNextLine()) { scanner.nextLine(); }

			while (scanner.hasNextLine())
			{
				sceneData.elements.add(parseSubscene(new Scanner(scanner.nextLine())));
			}
			parsed = true;
		}
		catch (Exception e) { out.sendException(e, "error.failed_to_load_scene"); }
	}

	public boolean isLegacy()
	{
		return legacy;
	}

	public boolean wasParsed()
	{
		return parsed;
	}

	private static SceneData.Element parseSubscene(Scanner scanner)
	{
		String elementName = scanner.next();
		MocapModifiers modifiers = PlaybackModifiers.EMPTY;
		try
		{
			modifiers = modifiers.withStartDelay(MocapStartDelay.fromSeconds(Double.parseDouble(scanner.next())));
			modifiers = modifiers.withTransformations(Transformations.fromLegacyScene(
					Double.parseDouble(scanner.next()), Double.parseDouble(scanner.next()), Double.parseDouble(scanner.next())));
			modifiers = modifiers.withPlayerName(parsePlayerName(scanner));
			modifiers = modifiers.withPlayerSkin(parsePlayerSkin(scanner));

			String playerAsEntityStr = scanner.next();
			if (playerAsEntityStr.equals(NULL_STR)) { playerAsEntityStr = null; }
			modifiers = modifiers.withPlayerAsEntity(new PlayerAsEntity(playerAsEntityStr, null));
		}
		catch (Exception ignore) {}

		return new SceneData.Element(elementName, modifiers);
	}

	private static @Nullable String parsePlayerName(Scanner scanner)
	{
		try
		{
			String nameStr = scanner.next();
			return (nameStr.length() <= 16 && !nameStr.equals(NULL_STR)) ? nameStr : null;
		}
		catch (Exception e) { return null; }
	}

	private static PlayerSkin parsePlayerSkin(Scanner scanner)
	{
		MocapPlayerSkin.Source source = MocapPlayerSkin.Source.DEFAULT;
		String skinPath = NULL_STR;

		try
		{
			skinPath = scanner.next();

			// Pre-1.3 compatibility
			if (!skinPath.equals(NULL_STR)) { source = MocapPlayerSkin.Source.FROM_MINESKIN; }

			source = skinSourceFromLegacyId(Integer.parseInt(scanner.next()));
		}
		catch (Exception ignore) {}

		return new PlayerSkin(source, skinPath.equals(NULL_STR) ? null : skinPath);
	}

	private static MocapPlayerSkin.Source skinSourceFromLegacyId(int id)
	{
		return switch (id)
		{
			case 1 -> MocapPlayerSkin.Source.FROM_PLAYER;
			case 2 -> MocapPlayerSkin.Source.FROM_FILE;
			case 3 -> MocapPlayerSkin.Source.FROM_MINESKIN;
			default -> MocapPlayerSkin.Source.DEFAULT;
		};
	}
}
