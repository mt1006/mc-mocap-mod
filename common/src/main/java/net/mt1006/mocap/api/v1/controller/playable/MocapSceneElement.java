package net.mt1006.mocap.api.v1.controller.playable;

import com.google.gson.JsonObject;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface MocapSceneElement
{
	String getName();

	MocapSceneElement withName(String name);

	MocapModifiers getModifiers();

	MocapSceneElement withModifiers(MocapModifiers modifiers);

	@Nullable MocapPlayable getPlayable(CommandInfo info);

	@ApiStatus.Internal
	JsonObject toJson();
}
