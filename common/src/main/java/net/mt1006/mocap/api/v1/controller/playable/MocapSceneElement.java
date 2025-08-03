package net.mt1006.mocap.api.v1.controller.playable;

import com.google.gson.JsonObject;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.mocap.playing.modifiers.PlaybackModifiers;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface MocapSceneElement
{
	String getName();

	MocapModifiers getModifiers();

	@Nullable MocapPlayable getPlayable(CommandInfo info);

	@ApiStatus.Internal
	JsonObject toJson();

	//TODO: remove
	@ApiStatus.Internal
	PlaybackModifiers getPlaybackModifiers();
}
