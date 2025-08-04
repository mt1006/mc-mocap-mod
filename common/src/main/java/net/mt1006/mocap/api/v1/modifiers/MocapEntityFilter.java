package net.mt1006.mocap.api.v1.modifiers;

import net.minecraft.world.entity.Entity;
import net.mt1006.mocap.mocap.playing.modifiers.EntityFilter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface MocapEntityFilter
{
	static MocapEntityFilter defaultForRecording()
	{
		return EntityFilter.FOR_RECORDING;
	}

	static MocapEntityFilter defaultForPlayback()
	{
		return EntityFilter.FOR_PLAYBACK;
	}

	boolean isAllowed(Entity entity);

	boolean isEmpty();

	boolean isDefaultForRecording();

	boolean isDefaultForPlayback();

	@Nullable String getFilterString();

	@ApiStatus.Internal
	@Nullable String save();
}
