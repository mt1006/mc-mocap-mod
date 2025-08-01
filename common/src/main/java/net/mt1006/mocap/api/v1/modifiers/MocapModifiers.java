package net.mt1006.mocap.api.v1.modifiers;

import net.mt1006.mocap.api.impl.modifiers.MocapModifiersImpl;
import net.mt1006.mocap.mocap.playing.modifiers.PlaybackModifiers;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface MocapModifiers
{
	static MocapModifiers empty()
	{
		return MocapModifiersImpl.empty();
	}

	@Nullable String getPlayerName();

	MocapModifiers setPlayerName(@Nullable String name);

	MocapPlayerSkin getPlayerSkin();

	MocapModifiers setPlayerSkin(MocapPlayerSkin skin);

	MocapTransformations getTransformations();

	MocapModifiers setTransformations(MocapTransformations transformations);

	MocapPlayerAsEntity getPlayerAsEntity();

	MocapModifiers setPlayerAsEntity(MocapPlayerAsEntity playerAsEntity);

	double getStartDelay();

	MocapModifiers setStartDelay(double seconds);

	MocapEntityFilter getEntityFilter();

	MocapModifiers setEntityFilter(@Nullable String filter);

	@ApiStatus.Internal
	PlaybackModifiers getPlaybackModifiers();
}
