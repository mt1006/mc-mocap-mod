package net.mt1006.mocap.mocap.playing.modifiers;

import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.api.v1.modifiers.MocapOffset;
import net.mt1006.mocap.api.v1.modifiers.MocapTransformationsConfig;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.files.SceneFiles;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class TransformationsConfig implements MocapTransformationsConfig
{
	public static final TransformationsConfig DEFAULT = new TransformationsConfig(false, RecordingCenter.AUTO, SceneCenter.DEFAULT, MocapOffset.ZERO);
	public static final TransformationsConfig LEGACY = new TransformationsConfig(true, RecordingCenter.AUTO, SceneCenter.DEFAULT, MocapOffset.ZERO);

	private final boolean roundBlockPos;
	private final RecordingCenter recordingCenter;
	private final SceneCenter sceneCenter;
	private final MocapOffset centerOffset;

	public TransformationsConfig(boolean roundBlockPos, RecordingCenter recordingCenter, SceneCenter sceneCenter, MocapOffset centerOffset)
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
		centerOffset = MocapOffset.fromVec3(reader.readVec3("center_offset"));
	}

	public static TransformationsConfig fromObject(@Nullable SceneFiles.Reader reader)
	{
		return reader != null ? new TransformationsConfig(reader) : DEFAULT;
	}

	@Override public boolean getRoundBlockPos()
	{
		return roundBlockPos;
	}

	@Override public TransformationsConfig withRoundBlockPos(boolean roundBlockPos)
	{
		return new TransformationsConfig(roundBlockPos, recordingCenter, sceneCenter, centerOffset);
	}

	@Override public RecordingCenter getRecordingCenter()
	{
		return recordingCenter;
	}

	@Override public TransformationsConfig withRecordingCenter(RecordingCenter center)
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

	@Override public TransformationsConfig withSceneCenter(SceneCenterType center, @Nullable String specificStr)
	{
		if (center != SceneCenterType.COMMON_SPECIFIC) { specificStr = null; }
		return new TransformationsConfig(roundBlockPos, recordingCenter, new SceneCenter(center, specificStr), centerOffset);
	}

	@Override public MocapOffset getCenterOffset()
	{
		return centerOffset;
	}

	@Override public TransformationsConfig withCenterOffset(MocapOffset offset)
	{
		return new TransformationsConfig(roundBlockPos, recordingCenter, sceneCenter, offset);
	}

	@Override public SceneCenter getSceneCenter()
	{
		return sceneCenter;
	}

	@Override public boolean isDefault()
	{
		return !roundBlockPos && recordingCenter == RecordingCenter.AUTO && centerOffset.isZero
				&& sceneCenter.type == SceneCenterType.COMMON_FIRST;
	}

	@Override public @Nullable SceneFiles.Writer save()
	{
		if (isDefault()) { return null; }

		SceneFiles.Writer writer = new SceneFiles.Writer();
		writer.addBoolean("round_block_pos", roundBlockPos, false);
		writer.addEnum("recording_center", recordingCenter, RecordingCenter.AUTO);
		writer.addObject("scene_center", sceneCenter.save());
		writer.addVec3("center_offset", centerOffset.save());

		return writer;
	}

	@Override public void list(CommandOutput out)
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

	@Override public @Nullable MocapTransformationsConfig modify(FullCommandInfo info, int propertyNodePos)
	{
		switch (info.getNode(propertyNodePos))
		{
			case "round_block_pos":
				return withRoundBlockPos(info.getBool("round"));

			case "recording_center":
				String centerPointStr = info.getNode(propertyNodePos + 1);
				return centerPointStr != null ? withRecordingCenter(RecordingCenter.valueOf(centerPointStr.toUpperCase(Locale.ROOT))) : null;

			case "scene_center":
				String sceneCenterStr = info.getNode(propertyNodePos + 1);
				if (sceneCenterStr == null) { return null; }

				SceneCenterType centerType = SceneCenterType.valueOf(sceneCenterStr.toUpperCase(Locale.ROOT));
				String specificStr = centerType == SceneCenterType.COMMON_SPECIFIC ? info.getString("specific_scene_element") : null;

				return withSceneCenter(centerType, specificStr);

			case "center_offset":
				return withCenterOffset(new Vec3(info.getDouble("offset_x"), info.getDouble("offset_y"), info.getDouble("offset_z")));

			case null, default:
				return null;
		}
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
					? String.format(Locale.ROOT, "%s [%s]", type.name(), specificStr)
					: type.name();
		}
	}
}
