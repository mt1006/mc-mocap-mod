package net.mt1006.mocap.mocap.playing.playback;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.impl.modifiers.MocapModifiersImpl;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.controller.playable.*;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.command.CommandUtils;
import net.mt1006.mocap.mocap.files.SceneData;
import net.mt1006.mocap.mocap.playing.PlaybackDataManager;
import net.mt1006.mocap.mocap.playing.modifiers.PlaybackModifiers;
import net.mt1006.mocap.mocap.playing.modifiers.Transformations;
import net.mt1006.mocap.mocap.playing.modifiers.TransformationsConfig;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ScenePlayback extends Playback
{
	private final List<Playback> subscenes;
	private final PositionTransformer transformer;

	private ScenePlayback(boolean isRoot, ServerLevel level, @Nullable ServerPlayer owner, MocapPlaybackConfig config,
						  PlaybackModifiers modifiers, List<Playback> subscenes, PositionTransformer transformer)
	{
		super(isRoot, level, owner, config, modifiers);
		this.subscenes = subscenes;
		this.transformer = transformer;
	}

	public static @Nullable ScenePlayback start(CommandInfo info, boolean isRoot, PlaybackDataManager dataManager, @Nullable SceneData sceneData,
												MocapPlaybackConfig config, PlaybackModifiers modifiers, @Nullable PositionTransformer parentTransformer)
	{
		if (sceneData == null) { return null; }

		if (sceneData.elements.isEmpty() && isRoot)
		{
			info.sendFailureWithTip("playback.start.error.empty_scene");
			return null;
		}

		PositionTransformer transformer = createPosTransformer(info, modifiers.transformations, parentTransformer, sceneData, dataManager);
		if (transformer == null) { return null; }

		List<Playback> subscenes = new ArrayList<>();
		for (MocapSceneElement element : sceneData.elements)
		{
			MocapPlayable playable = element.getPlayable(info);
			if (playable == null) { return null; }

			PlaybackModifiers subsceneModifiers = element.getPlaybackModifiers().mergeWithParent(modifiers);
			Playback playback = playable.startAsSubscene(info, MocapModifiersImpl.ofCopy(subsceneModifiers), config, dataManager, transformer);
			if (playback == null) { return null; }
			subscenes.add(playback);
		}

		return new ScenePlayback(isRoot, info.getLevel(), info.getSourcePlayer(), config, modifiers, subscenes, transformer);
	}

	private static @Nullable Vec3 getSceneStartPos(CommandInfo info, TransformationsConfig.SceneCenter centers,
												   SceneData sceneData, PlaybackDataManager dataManager)
	{
		TransformationsConfig.SceneCenterType centerType = centers.type;
		if (centerType == TransformationsConfig.SceneCenterType.COMMON_SPECIFIC && centers.specificStr == null)
		{
			centerType = TransformationsConfig.SceneCenterType.COMMON_FIRST;
		}

		MocapSceneElement element = switch (centerType)
		{
			case COMMON_FIRST -> sceneData.elements.get(0);
			case COMMON_LAST -> sceneData.elements.get(sceneData.elements.size() - 1);
			case COMMON_SPECIFIC -> SceneData.loadSubscene(info, sceneData, CommandUtils.splitPosStr(centers.specificStr));
			default -> null;
		};
		if (element == null) { return null; }

		Vec3 subsceneStartPos = switch (element.getPlayable(info))
		{
			case MocapSceneFile sceneFile -> getSceneStartPos(info,
					element.getPlaybackModifiers().transformations.config.getSceneCenter(), dataManager.getScene(sceneFile), dataManager);
			case MocapRecordingFile recordingFile -> dataManager.getRecording(recordingFile).startPos;
			case MocapActiveRecording activeRecording -> activeRecording.getRecordingData().startPos;
			case null -> throw new NullPointerException("Playable is null");
			default -> throw new IllegalStateException("Unexpected value: " + element.getPlayable(info));
		};
		return element.getPlaybackModifiers().transformations.calculateCenter(subsceneStartPos);
	}

	private static @Nullable PositionTransformer createPosTransformer(CommandInfo info, Transformations transformations,
																	  @Nullable PositionTransformer parent, SceneData sceneData,
																	  PlaybackDataManager dataManager)
	{
		if (parent != null && transformations.areDefault()) { return parent; }
		TransformationsConfig.SceneCenter center = transformations.config.getSceneCenter();

		if (center.type == TransformationsConfig.SceneCenterType.INDIVIDUAL || sceneData.elements.isEmpty())
		{
			return new PositionTransformer(transformations, parent, null);
		}

		Vec3 sceneStartPos = getSceneStartPos(info, center, sceneData, dataManager);
		if (sceneStartPos == null) { return null; }

		return new PositionTransformer(transformations, parent, sceneStartPos);
	}

	@Override public boolean tick()
	{
		if (finished) { return true; }

		if (shouldExecuteTick())
		{
			finished = true;
			for (Playback scene : subscenes)
			{
				if (!scene.tick()) { finished = false; }
			}
		}

		if (isRoot && finished) { stop(); }

		tickCounter++;
		return finished;
	}

	@Override public void stop()
	{
		subscenes.forEach(Playback::stop);
		finished = true;
	}

	@Override public boolean wasFinished()
	{
		return finished;
	}

	@Override protected PositionTransformer getPosTransformer()
	{
		return transformer;
	}
}
