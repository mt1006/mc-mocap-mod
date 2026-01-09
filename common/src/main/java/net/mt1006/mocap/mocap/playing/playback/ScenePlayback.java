package net.mt1006.mocap.mocap.playing.playback;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.controller.playable.*;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.api.v1.modifiers.MocapTransformations;
import net.mt1006.mocap.command.CommandUtils;
import net.mt1006.mocap.mocap.files.SceneData;
import net.mt1006.mocap.mocap.playing.PlaybackDataManager;
import net.mt1006.mocap.mocap.playing.modifiers.TransformationsConfig;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ScenePlayback extends Playback
{
	private final List<Playback> subscenes;

	private ScenePlayback(boolean isRoot, @Nullable ServerPlayer owner, MocapPlaybackConfig config,
						  MocapModifiers modifiers, List<Playback> subscenes)
	{
		super(isRoot, owner, config, modifiers);
		this.subscenes = subscenes;
	}

	public static @Nullable ScenePlayback start(CommandInfo info, boolean isRoot, PlaybackDataManager dataManager, @Nullable SceneData sceneData,
												MocapPlaybackConfig config, MocapModifiers modifiers, @Nullable PositionTransformer parentTransformer)
	{
		if (sceneData == null) { return null; }

		if (sceneData.elements.isEmpty() && isRoot)
		{
			info.sendFailureWithTip("playback.start.error.empty_scene");
			return null;
		}

		PositionTransformer transformer = createPosTransformer(info, modifiers.getTransformations(), parentTransformer, sceneData, dataManager);
		if (transformer == null) { return null; }

		List<Playback> subscenes = new ArrayList<>();
		for (MocapSceneElement element : sceneData.elements)
		{
			MocapPlayable playable = element.getPlayable(info);
			if (playable == null) { return null; }

			MocapModifiers subsceneModifiers = element.getModifiers().mergeWithParent(modifiers);
			Playback playback = playable.startAsSubscene(info, subsceneModifiers, config, dataManager, transformer);
			if (playback == null) { return null; }
			subscenes.add(playback);
		}

		return new ScenePlayback(isRoot, info.getSourcePlayer(), config, modifiers, subscenes);
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
					element.getModifiers().getTransformations().getConfig().getSceneCenter(), dataManager.getScene(sceneFile), dataManager);
			case MocapRecordingFile recordingFile -> dataManager.getRecording(recordingFile).getStartPos();
			case MocapActiveRecording activeRecording -> activeRecording.getContext().getRecordingData().getStartPos();
			case null -> throw new NullPointerException("Playable is null");
			default -> throw new IllegalStateException("Unexpected value: " + element.getPlayable(info));
		};
		return element.getModifiers().getTransformations().calculateCenter(subsceneStartPos);
	}

	private static @Nullable PositionTransformer createPosTransformer(CommandInfo info, MocapTransformations transformations,
																	  @Nullable PositionTransformer parent, SceneData sceneData,
																	  PlaybackDataManager dataManager)
	{
		if (parent != null && transformations.areDefault()) { return parent; }
		TransformationsConfig.SceneCenter center = transformations.getConfig().getSceneCenter();

		if (center.type == TransformationsConfig.SceneCenterType.INDIVIDUAL || sceneData.elements.isEmpty())
		{
			return new PositionTransformer(transformations, parent, null);
		}

		Vec3 sceneStartPos = getSceneStartPos(info, center, sceneData, dataManager);
		if (sceneStartPos == null) { return null; }

		return new PositionTransformer(transformations, parent, sceneStartPos);
	}

	@Override public void tick()
	{
		if (finished) { return; }

		if (shouldExecuteTick())
		{
			if (waitOnEnd != 0)
			{
				if (waitOnEnd == 1) { finished = true; }
				waitOnEnd--;
			}
			else
			{
				boolean subscenesInactive = true, subscenesStopped = true;
				for (Playback scene : subscenes)
				{
					scene.tick();
					if (scene.isActive()) { subscenesInactive = false; }
					if (!scene.stopped) { subscenesStopped = false; }
				}

				if (subscenesInactive) { finishOrWaitOnEnd(); }
				if (subscenesStopped) { stop(); }
			}
			tickCounter++;
		}

		if (finished && modifiers.getTimeModifiers().getLoop()) { loop(); }
		else if (shouldSelfStop()) { stop(); }
	}

	@Override public void stop()
	{
		if (!stopped)
		{
			subscenes.forEach(Playback::stop);
			finished = true;
			stopped = true;
		}
	}

	@Override protected void loop()
	{
		subscenes.forEach(Playback::loop);
		tickCounter = 0;
		finished = false;
	}
}
