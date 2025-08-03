package net.mt1006.mocap.api.impl.modifiers;

import net.mt1006.mocap.api.v1.modifiers.*;
import net.mt1006.mocap.mocap.playing.modifiers.EntityFilter;
import net.mt1006.mocap.mocap.playing.modifiers.PlaybackModifiers;
import net.mt1006.mocap.mocap.playing.modifiers.StartDelay;
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

	@Override public PlaybackModifiers getPlaybackModifiers()
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
		return modify((m) -> m.playerSkin = skin);
	}

	@Override public MocapTransformations getTransformations()
	{
		return MocapTransformationsImpl.ofCopy(modifiers.transformations);
	}

	@Override public MocapModifiers setTransformations(MocapTransformations transformations)
	{
		return modify((m) -> m.transformations = transformations.getCopy());
	}

	@Override public MocapPlayerAsEntity getPlayerAsEntity()
	{
		return modifiers.playerAsEntity;
	}

	@Override public MocapModifiers setPlayerAsEntity(MocapPlayerAsEntity playerAsEntity)
	{
		return modify((m) -> m.playerAsEntity = playerAsEntity);
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
