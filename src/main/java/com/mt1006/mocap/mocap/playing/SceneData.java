package com.mt1006.mocap.mocap.playing;

import com.mt1006.mocap.utils.FileUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SceneData
{
	private Map<String, byte[]> recordingsMap = new HashMap<>();
	private Map<String, SceneInfo> scenesMap = new HashMap<>();
	private Stack<String> resourcesStack = new Stack<>();

	public boolean load(CommandSourceStack commandSource, String name)
	{
		if (name.charAt(0) == '.')
		{
			if (resourcesStack.contains(name))
			{
				commandSource.sendFailure(new TranslatableComponent("mocap.commands.playing.start.failed.loop"));
				return false;
			}

			resourcesStack.push(name);

			if (!loadResource(commandSource, name)) { return false; }
			for (SceneInfo.Subscene subscene : getScene(name).subscenes)
			{
				if (!load(commandSource, subscene.getName())) { return false; }
			}

			resourcesStack.pop();
			return true;
		}
		else
		{
			return loadResource(commandSource, name);
		}
	}

	@Nullable
	public byte[] getRecording(String name)
	{
		return recordingsMap.get(name);
	}

	@Nullable
	public SceneInfo getScene(String name)
	{
		return scenesMap.get(name);
	}

	private boolean loadResource(CommandSourceStack commandSource, String name)
	{
		if (name.charAt(0) == '.')
		{
			if (scenesMap.containsKey(name)) { return true; }
			SceneInfo sceneInfo = new SceneInfo();
			if(!sceneInfo.load(commandSource, name.substring(1))) { return false; }
			scenesMap.put(name, sceneInfo);
		}
		else
		{
			if (recordingsMap.containsKey(name)) { return true; }
			byte[] recording = FileUtils.loadRecording(commandSource, name);
			if (recording == null) { return false; }
			recordingsMap.put(name, recording);
		}

		return true;
	}
}
