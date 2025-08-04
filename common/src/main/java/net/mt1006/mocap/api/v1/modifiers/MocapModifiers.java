package net.mt1006.mocap.api.v1.modifiers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.files.SceneFiles;
import net.mt1006.mocap.mocap.playing.modifiers.PlaybackModifiers;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface MocapModifiers
{
	static MocapModifiers empty()
	{
		return PlaybackModifiers.EMPTY;
	}

	static MocapModifiers playerName(@Nullable String name)
	{
		return PlaybackModifiers.EMPTY.withPlayerName(name);
	}

	@Nullable String getPlayerName();

	MocapModifiers withPlayerName(@Nullable String name);

	MocapPlayerSkin getPlayerSkin();

	MocapModifiers withPlayerSkin(MocapPlayerSkin skin);

	MocapTransformations getTransformations();

	MocapModifiers withTransformations(MocapTransformations transformations);

	MocapPlayerAsEntity getPlayerAsEntity();

	MocapModifiers withPlayerAsEntity(MocapPlayerAsEntity playerAsEntity);

	MocapStartDelay getStartDelay();

	MocapModifiers withStartDelay(MocapStartDelay startDelay);

	MocapEntityFilter getEntityFilter();

	MocapModifiers withEntityFilter(MocapEntityFilter filter);

	@ApiStatus.Internal
	boolean areDefault();

	@ApiStatus.Internal
	MocapModifiers mergeWithParent(MocapModifiers parent);

	@ApiStatus.Internal
	void save(SceneFiles.Writer writer);

	@ApiStatus.Internal
	void list(CommandOutput out);

	@ApiStatus.Internal
	@Nullable MocapModifiers modify(FullCommandInfo info, String propertyName, int propertyNodePosition) throws CommandSyntaxException;
}
