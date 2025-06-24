package net.mt1006.mocap.api.v1.modifiers;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface MocapTransformationsConfig
{
	boolean getRoundBlockPos();

	MocapTransformationsConfig setRoundBlockPos(boolean roundBlockPos);

	RecordingCenter getRecordingCenter();

	MocapTransformationsConfig setRecordingCenter(RecordingCenter center);

	SceneCenterType getSceneCenterType();

	@Nullable String getSceneCenterSpecificStr();

	MocapTransformationsConfig setSceneCenter(SceneCenterType center, @Nullable String specificStr);

	Vec3 getCenterOffset();

	MocapTransformationsConfig setCenterOffset(Vec3 offset);

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
