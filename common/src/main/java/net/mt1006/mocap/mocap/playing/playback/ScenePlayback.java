package net.mt1006.mocap.mocap.playing.playback;

import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.command.CommandUtils;
import net.mt1006.mocap.command.io.CommandInfo;
import net.mt1006.mocap.command.io.CommandOutput;
import net.mt1006.mocap.mocap.files.RecordingData;
import net.mt1006.mocap.mocap.files.SceneData;
import net.mt1006.mocap.mocap.playing.DataManager;
import net.mt1006.mocap.mocap.playing.modifiers.PlaybackModifiers;
import net.mt1006.mocap.mocap.playing.modifiers.TransformationsConfig;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ScenePlayback extends Playback
{
	private final List<Playback> subscenes = new ArrayList<>();
	private final PositionTransformer transformer;

	private ScenePlayback(CommandInfo commandInfo, DataManager dataManager, String name, MocapPlaybackConfig config, PlaybackModifiers parentModifiers,
						  @Nullable SceneData.Subscene info, @Nullable PositionTransformer parentTransformer) throws StartException
	{
		super(info == null, commandInfo.getLevel(), commandInfo.getSourcePlayer(), config, parentModifiers, info);

		SceneData sceneData = dataManager.getScene(name);
		if (sceneData == null) { throw new StartException(); }

		if (sceneData.subscenes.isEmpty() && root)
		{
			commandInfo.sendFailureWithTip("playback.start.error.empty_scene");
			throw new StartException();
		}

		transformer = createPosTransformer(commandInfo, parentTransformer, sceneData, dataManager);
		for (SceneData.Subscene subscene : sceneData.subscenes)
		{
			Playback playback = Playback.start(commandInfo, dataManager, config, this, subscene);
			if (playback == null) { return; }
			subscenes.add(playback);
		}
	}

	protected static @Nullable ScenePlayback startRoot(CommandInfo commandInfo, DataManager dataManager,
													   MocapPlaybackConfig config, String name, PlaybackModifiers modifiers)
	{
		try { return new ScenePlayback(commandInfo, dataManager, name, config, modifiers, null, null); }
		catch (StartException e) { return null; }
	}

	protected static @Nullable ScenePlayback startSubscene(CommandInfo commandInfo, DataManager dataManager,
														   MocapPlaybackConfig config, Playback parent, SceneData.Subscene info)
	{
		try { return new ScenePlayback(commandInfo, dataManager, info.name, config, parent.modifiers, info, parent.getPosTransformer()); }
		catch (StartException e) { return null; }
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

		if (root && finished) { stop(); }

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

	private PositionTransformer createPosTransformer(CommandOutput commandOutput, @Nullable PositionTransformer parent,
															   SceneData sceneData, DataManager dataManager) throws StartException
	{
		if (modifiers.transformations.areDefault()) { return parent; }
		TransformationsConfig.SceneCenter center = modifiers.transformations.config.sceneCenter;

		if (center.type == TransformationsConfig.SceneCenterType.INDIVIDUAL || sceneData.subscenes.isEmpty())
		{
			return new PositionTransformer(modifiers.transformations, parent, null);
		}

		Vec3 sceneStartPos = getSceneStartPos(commandOutput, center, sceneData, dataManager);
		return new PositionTransformer(modifiers.transformations, parent, sceneStartPos);
	}

	private static Vec3 getSceneStartPos(CommandOutput commandOutput, TransformationsConfig.SceneCenter centers,
										 SceneData sceneData, DataManager dataManager) throws StartException
	{
		TransformationsConfig.SceneCenterType centerType = centers.type;
		if (centerType == TransformationsConfig.SceneCenterType.COMMON_SPECIFIC && centers.specificStr == null)
		{
			centerType = TransformationsConfig.SceneCenterType.COMMON_FIRST;
		}

		SceneData.Subscene subscene = switch (centerType)
		{
			case COMMON_FIRST -> sceneData.subscenes.get(0);
			case COMMON_LAST -> sceneData.subscenes.get(sceneData.subscenes.size() - 1);
			case COMMON_SPECIFIC -> SceneData.loadSubscene(commandOutput, sceneData, CommandUtils.splitPosStr(centers.specificStr));
			default -> null;
		};
		if (subscene == null) { throw new StartException(); }

		Vec3 subsceneStartPos;
		if (subscene.name.startsWith("."))
		{
			SceneData subsceneData = dataManager.getScene(subscene.name);
			subsceneStartPos = getSceneStartPos(commandOutput,
					subscene.modifiers.transformations.config.sceneCenter, subsceneData, dataManager);
		}
		else
		{
			RecordingData recording = dataManager.getRecording(subscene.name);
			if (recording == null) { throw new StartException(); }
			subsceneStartPos = recording.startPos;
		}

		return subscene.modifiers.transformations.calculateCenter(subsceneStartPos);
	}
}
