package net.mt1006.mocap.mocap.playing;

import net.mt1006.mocap.api.v1.controller.playable.MocapRecordingFile;
import net.mt1006.mocap.api.v1.controller.playable.MocapSceneElement;
import net.mt1006.mocap.api.v1.controller.playable.MocapSceneFile;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.mocap.files.RecordingData;
import net.mt1006.mocap.mocap.files.SceneData;
import net.mt1006.mocap.mocap.playing.playable.Playable;
import net.mt1006.mocap.mocap.playing.playable.RecordingFile;
import net.mt1006.mocap.mocap.playing.playable.SceneFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class PlaybackDataManager
{
	private final Map<MocapRecordingFile, RecordingData> recordingMap = new HashMap<>();
	private final Map<MocapSceneFile, SceneData> sceneMap = new HashMap<>();
	private final Stack<Playable> resourceStack = new Stack<>();

	public boolean loadRecording(CommandOutput out, RecordingFile file)
	{
		if (recordingMap.containsKey(file)) { return true; }
		resourceStack.push(file);

		RecordingData recording = new RecordingData();
		if (!recording.load(out, file)) { return sendFailureWithPath(out); }
		recordingMap.put(file, recording);

		resourceStack.pop();
		return true;
	}

	public boolean loadScene(CommandOutput out, SceneFile file)
	{
		if (sceneMap.containsKey(file)) { return true; }

		boolean hasResourceLoop = resourceStack.contains(file);
		resourceStack.push(file);
		if (hasResourceLoop) { return sendFailureWithPath(out, "playback.start.error.loop"); }

		SceneData sceneData = new SceneData();
		if (!sceneData.load(out, file)) { return sendFailureWithPath(out); }
		sceneMap.put(file, sceneData);

		for (MocapSceneElement element : sceneData.elements)
		{
			if (!loadFromName(out, element.getName())) { return false; }
		}

		resourceStack.pop();
		return true;
	}

	private boolean loadFromName(CommandOutput out, String name)
	{
		//TODO: add support for active recordings/virtual
		if (name.charAt(0) == '.')
		{
			SceneFile file = SceneFile.get(out, name);
			return file != null && loadScene(out, file);
		}
		else
		{
			RecordingFile file = RecordingFile.get(out, name);
			return file != null && loadRecording(out, file);
		}
	}

	private boolean sendFailureWithPath(CommandOutput out)
	{
		return sendFailureWithPath(out, "playback.start.error.load");
	}

	private boolean sendFailureWithPath(CommandOutput out, String component)
	{
		StringBuilder retStr = new StringBuilder();
		for (Playable playable : resourceStack)
		{
			retStr.append("/");
			retStr.append(playable.getName());
		}
		String path = retStr.toString();

		out.sendFailure(component);
		out.sendFailure("playback.start.error.load.path", path);
		return false;
	}

	// this and other queries to map are not marked as nullable, as they should've been preloaded by "load" methods
	public RecordingData getRecording(MocapRecordingFile file)
	{
		return recordingMap.get(file);
	}

	public SceneData getScene(MocapSceneFile file)
	{
		return sceneMap.get(file);
	}
}
