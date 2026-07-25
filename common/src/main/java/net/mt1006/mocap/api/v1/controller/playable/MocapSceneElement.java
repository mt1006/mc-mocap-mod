package net.mt1006.mocap.api.v1.controller.playable;

import com.google.gson.JsonObject;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface MocapSceneElement
{
	/**
	 * @return name of a playable referenced by scene element
	 */
	String getName();

	/**
	 * @return copy of a scene element with a given name (reference to playable)
	 */
	MocapSceneElement withName(String name);

	/**
	 * @return playback modifiers of a scene element
	 * @see MocapModifiers
	 */
	MocapModifiers getModifiers();

	/**
	 * @return copy of a scene element with given playback modifiers
	 * @see MocapModifiers
	 */
	MocapSceneElement withModifiers(MocapModifiers modifiers);

	/**
	 * @param info log output and command context
	 * @return reference to a playable file/object from scene element name, null if it wasn't found
	 */
	@Nullable MocapPlayable getPlayable(CommandInfo info);

	@ApiStatus.Internal
	JsonObject toJson();
}
