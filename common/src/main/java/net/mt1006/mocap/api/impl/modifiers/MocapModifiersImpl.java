package net.mt1006.mocap.api.impl.modifiers;

import net.mt1006.mocap.api.v1.modifiers.*;
import net.mt1006.mocap.mocap.playing.modifiers.*;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class MocapModifiersImpl implements MocapModifiers
{
	private final PlaybackModifiers modifiers;

	private MocapModifiersImpl(PlaybackModifiers modifiers)
	{
		this.modifiers = modifiers;
	}

	public static MocapModifiersImpl ofCopy(PlaybackModifiers modifiers)
	{
		return new MocapModifiersImpl(modifiers.copy());
	}

	public static MocapModifiersImpl empty()
	{
		return new MocapModifiersImpl(PlaybackModifiers.empty());
	}

	public static MocapModifiersImpl withPlayerName(@Nullable String name)
	{
		PlaybackModifiers modifiers = PlaybackModifiers.empty();
		modifiers.playerName = name;
		return new MocapModifiersImpl(modifiers);
	}

	private MocapModifiersImpl modify(Consumer<PlaybackModifiers> modifier)
	{
		PlaybackModifiers copy = modifiers.copy();
		modifier.accept(copy);
		return new MocapModifiersImpl(copy);
	}

	public PlaybackModifiers getPlaybackModifiers()
	{
		return modifiers;
	}

	@Override public @Nullable String getPlayerName()
	{
		return modifiers.playerName;
	}

	@Override public MocapModifiers setPlayerName(@Nullable String name)
	{
		return modify((m) -> m.playerName = name);
	}

	@Override public MocapPlayerSkin getPlayerSkin()
	{
		return modifiers.playerSkin;
	}

	@Override public MocapModifiers setPlayerSkin(MocapPlayerSkin skin)
	{
		if (!(skin instanceof PlayerSkin)) { throw new RuntimeException("MocapPlayerSkin isn't instance of PlayerSkin!"); }
		return modify((m) -> m.playerSkin = (PlayerSkin)skin);
	}

	@Override public MocapTransformations getTransformations()
	{
		return MocapTransformationsImpl.ofCopy(modifiers.transformations);
	}

	@Override public MocapModifiers setTransformations(MocapTransformations transformations)
	{
		if (!(transformations instanceof MocapTransformationsImpl)) { throw new RuntimeException("MocapTransformations isn't instance of MocapTransformationsImpl!"); }
		return modify((m) -> m.transformations = ((MocapTransformationsImpl)transformations).getCopy());
	}

	@Override public MocapPlayerAsEntity getPlayerAsEntity()
	{
		return modifiers.playerAsEntity;
	}

	@Override public MocapModifiers setPlayerAsEntity(MocapPlayerAsEntity playerAsEntity)
	{
		if (!(playerAsEntity instanceof PlayerAsEntity)) { throw new RuntimeException("MocapPlayerAsEntity isn't instance of PlayerAsEntity!"); }
		return modify((m) -> m.playerAsEntity = (PlayerAsEntity)playerAsEntity);
	}

	@Override public double getStartDelay()
	{
		return modifiers.startDelay.seconds;
	}

	@Override public MocapModifiers setStartDelay(double seconds)
	{
		return modify((m) -> m.startDelay = StartDelay.fromSeconds(seconds));
	}

	@Override public MocapEntityFilter getEntityFilter()
	{
		return modifiers.entityFilter;
	}

	@Override public MocapModifiers setEntityFilter(@Nullable String filter)
	{
		return modify((m) -> m.entityFilter = EntityFilter.fromString(filter));
	}
}
