package net.mt1006.mocap.api.v1.modifiers;

import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.files.SceneFiles;
import net.mt1006.mocap.mocap.playing.modifiers.TransformationsConfig;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface MocapTransformationsConfig
{
	boolean getRoundBlockPos();

	MocapTransformationsConfig withRoundBlockPos(boolean roundBlockPos);

	RecordingCenter getRecordingCenter();

	MocapTransformationsConfig withRecordingCenter(RecordingCenter center);

	SceneCenterType getSceneCenterType();

	@Nullable String getSceneCenterSpecificStr();

	MocapTransformationsConfig withSceneCenter(SceneCenterType center, @Nullable String specificStr);

	MocapOffset getCenterOffset();

	default MocapTransformationsConfig withCenterOffset(Vec3 offset)
	{
		return withCenterOffset(MocapOffset.fromVec3(offset));
	}

	MocapTransformationsConfig withCenterOffset(MocapOffset offset);

	@ApiStatus.Internal
	TransformationsConfig.SceneCenter getSceneCenter();

	@ApiStatus.Internal
	boolean isDefault();

	@ApiStatus.Internal
	@Nullable SceneFiles.Writer save();

	@ApiStatus.Internal
	void list(CommandOutput out);

	@ApiStatus.Internal
	@Nullable MocapTransformationsConfig modify(FullCommandInfo info, String propertyName, int propertyNodePos);

	enum RecordingCenter
	{
		AUTO,
		BLOCK_CENTER,
		BLOCK_CORNER,
		ACTUAL
	}

	enum SceneCenterType
	{
		COMMON_FIRST,
		COMMON_LAST,
		COMMON_SPECIFIC,
		INDIVIDUAL
	}
}
