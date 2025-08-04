package net.mt1006.mocap.mocap.playing.modifiers;

import net.minecraft.world.entity.Entity;
import net.mt1006.mocap.api.v1.modifiers.MocapEntityFilter;
import org.jetbrains.annotations.Nullable;

public class EntityFilter implements MocapEntityFilter
{
	public static final EntityFilter FOR_RECORDING = new EntityFilter(null);
	public static final EntityFilter FOR_PLAYBACK = new EntityFilter(null);

	private static @Nullable EntityFilterInstance recordingSettingInstance, playbackSettingInstance;
	private final @Nullable EntityFilterInstance constantInstance;

	public EntityFilter(@Nullable EntityFilterInstance instance)
	{
		constantInstance = instance;
	}

	public static EntityFilter fromString(@Nullable String str)
	{
		//TODO: test invalid filter
		return str != null ? new EntityFilter(EntityFilterInstance.create(str)) : FOR_PLAYBACK;
	}

	@Override public @Nullable String save()
	{
		if (this == FOR_PLAYBACK) { return null; }
		if (this == FOR_RECORDING) { throw new RuntimeException("Trying to save FOR_RECORDING EntityFilter!"); }
		if (constantInstance == null) { throw new RuntimeException("Trying to save constant instance, but constantInstance is null!"); }
		return constantInstance.filterStr;
	}

	@Override public boolean isAllowed(Entity entity)
	{
		EntityFilterInstance instance = getInstance();
		return instance != null && instance.isAllowed(entity);
	}

	@Override public boolean isEmpty()
	{
		EntityFilterInstance instance = getInstance();
		return instance == null || instance.isEmpty();
	}

	@Override public boolean isDefaultForRecording()
	{
		return this == FOR_RECORDING;
	}

	@Override public boolean isDefaultForPlayback()
	{
		return this == FOR_PLAYBACK;
	}

	@Override public @Nullable String getFilterString()
	{
		EntityFilterInstance instance = getInstance();
		return instance != null ? instance.filterStr : null;
	}

	private @Nullable EntityFilterInstance getInstance()
	{
		if (this == FOR_RECORDING) { return recordingSettingInstance; }
		if (this == FOR_PLAYBACK) { return playbackSettingInstance; }
		return constantInstance;
	}

	public static void onTrackEntitiesSet(String str)
	{
		recordingSettingInstance = EntityFilterInstance.create(str);
	}

	public static void onPlaybackEntitiesSet(String str)
	{
		playbackSettingInstance = EntityFilterInstance.create(str);
	}
}
