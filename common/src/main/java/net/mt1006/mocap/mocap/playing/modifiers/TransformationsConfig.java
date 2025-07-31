package net.mt1006.mocap.mocap.playing.modifiers;

import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.modifiers.MocapTransformationsConfig;
import net.mt1006.mocap.command.io.CommandOutput;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.files.SceneFiles;
import org.jetbrains.annotations.Nullable;

public class TransformationsConfig implements MocapTransformationsConfig
{
	public static final TransformationsConfig DEFAULT = new TransformationsConfig(false, RecordingCenter.AUTO, SceneCenter.DEFAULT, Offset.ZERO);
	public static final TransformationsConfig LEGACY = new TransformationsConfig(true, RecordingCenter.AUTO, SceneCenter.DEFAULT, Offset.ZERO);

	public final boolean roundBlockPos;
	public final RecordingCenter recordingCenter;
	public final SceneCenter sceneCenter;
	public final Offset centerOffset;

	public TransformationsConfig(boolean roundBlockPos, RecordingCenter recordingCenter, SceneCenter sceneCenter, Offset centerOffset)
	{
		this.roundBlockPos = roundBlockPos;
		this.recordingCenter = recordingCenter;
		this.sceneCenter = sceneCenter;
		this.centerOffset = centerOffset;
	}

	public TransformationsConfig(SceneFiles.Reader reader)
	{
		roundBlockPos = reader.readBoolean("round_block_pos", false);
		recordingCenter = reader.readEnum("recording_center", RecordingCenter.AUTO);
		sceneCenter = SceneCenter.fromObject(reader.readObject("scene_center"));
		centerOffset = Offset.fromVec3(reader.readVec3("center_offset"));
	}

	public static TransformationsConfig fromObject(@Nullable SceneFiles.Reader reader)
	{
		return reader != null ? new TransformationsConfig(reader) : DEFAULT;
	}

	@Override public boolean getRoundBlockPos()
	{
		return roundBlockPos;
	}

	@Override public TransformationsConfig setRoundBlockPos(boolean roundBlockPos)
	{
		return new TransformationsConfig(roundBlockPos, recordingCenter, sceneCenter, centerOffset);
	}

	@Override public RecordingCenter getRecordingCenter()
	{
		return recordingCenter;
	}

	@Override public TransformationsConfig setRecordingCenter(RecordingCenter center)
	{
		return new TransformationsConfig(roundBlockPos, center, sceneCenter, centerOffset);
	}

	@Override public SceneCenterType getSceneCenterType()
	{
		return sceneCenter.type;
	}

	@Override public @Nullable String getSceneCenterSpecificStr()
	{
		return sceneCenter.specificStr;
	}

	@Override public TransformationsConfig setSceneCenter(SceneCenterType center, @Nullable String specificStr)
	{
		if (center != SceneCenterType.COMMON_SPECIFIC) { specificStr = null; }
		return new TransformationsConfig(roundBlockPos, recordingCenter, new SceneCenter(center, specificStr), centerOffset);
	}

	@Override public Vec3 getCenterOffset()
	{
		return centerOffset;
	}

	@Override public TransformationsConfig setCenterOffset(Vec3 offset)
	{
		return new TransformationsConfig(roundBlockPos, recordingCenter, sceneCenter, new Offset(offset.x, offset.y, offset.z));
	}

	public boolean isDefault()
	{
		return !roundBlockPos && recordingCenter == RecordingCenter.AUTO && centerOffset.isZero
				&& sceneCenter.type == SceneCenterType.COMMON_FIRST;
	}

	public @Nullable SceneFiles.Writer save()
	{
		if (isDefault()) { return null; }

		SceneFiles.Writer writer = new SceneFiles.Writer();
		writer.addBoolean("round_block_pos", roundBlockPos, false);
		writer.addEnum("recording_center", recordingCenter, RecordingCenter.AUTO);
		writer.addObject("scene_center", sceneCenter.save());
		writer.addVec3("center_offset", centerOffset.save());

		return writer;
	}

	public void list(CommandOutput out)
	{
		if (isDefault())
		{
			out.sendSuccess("scenes.element_info.transformations.default_config");
			return;
		}

		out.sendSuccess("scenes.element_info.transformations.round_block_pos." + roundBlockPos);
		out.sendSuccess("scenes.element_info.transformations.recording_center", recordingCenter.name());
		out.sendSuccess("scenes.element_info.transformations.scene_center", sceneCenter.toString());
		out.sendSuccess("scenes.element_info.transformations.center_offset", centerOffset.x, centerOffset.y, centerOffset.z);
	}

	public @Nullable TransformationsConfig modify(FullCommandInfo info, String propertyName, int propertyNodePosition)
	{
		switch (propertyName)
		{
			case "round_block_pos":
				return setRoundBlockPos(info.getBool("round"));

			case "recording_center":
				String centerPointStr = info.getNode(propertyNodePosition + 1);
				if (centerPointStr == null) { break; }

				return setRecordingCenter(RecordingCenter.valueOf(centerPointStr.toUpperCase()));

			case "scene_center":
				String sceneCenterStr = info.getNode(propertyNodePosition + 1);
				if (sceneCenterStr == null) { break; }

				SceneCenterType centerType = SceneCenterType.valueOf(sceneCenterStr.toUpperCase());
				String specificStr = centerType == SceneCenterType.COMMON_SPECIFIC
						? info.getString("specific_scene_element") : null;

				return setSceneCenter(centerType, specificStr);

			case "center_offset":
				return setCenterOffset(new Vec3(info.getDouble("offset_x"),
						info.getDouble("offset_y"), info.getDouble("offset_z")));
		}
		return null;
	}

	public static class SceneCenter
	{
		public static final SceneCenter DEFAULT = new SceneCenter(SceneCenterType.COMMON_FIRST, null);
		public final SceneCenterType type;
		public final @Nullable String specificStr;

		public SceneCenter(SceneCenterType type, @Nullable String specificStr)
		{
			this.type = type;
			this.specificStr = specificStr;
		}

		public SceneCenter(SceneFiles.Reader reader)
		{
			type = reader.readEnum("type", SceneCenterType.COMMON_FIRST);
			specificStr = (type == SceneCenterType.COMMON_SPECIFIC) ? reader.readString("specific_str") : null;
		}

		public static SceneCenter fromObject(@Nullable SceneFiles.Reader reader)
		{
			return reader != null ? new SceneCenter(reader) : DEFAULT;
		}

		public @Nullable SceneFiles.Writer save()
		{
			if (type == SceneCenterType.COMMON_FIRST) { return null; }

			SceneFiles.Writer writer = new SceneFiles.Writer();
			writer.addEnum("type", type, SceneCenterType.COMMON_FIRST);
			if (type == SceneCenterType.COMMON_SPECIFIC) { writer.addString("specific_str", specificStr); }

			return writer;
		}

		@Override public String toString()
		{
			return (type == SceneCenterType.COMMON_SPECIFIC)
					? String.format("%s [%s]", type.name(), specificStr)
					: type.name();
		}
	}
}
