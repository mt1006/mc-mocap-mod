package net.mt1006.mocap.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.mt1006.mocap.command.io.CommandOutput;
import net.mt1006.mocap.mocap.files.Files;
import net.mt1006.mocap.mocap.files.RecordingFiles;
import net.mt1006.mocap.mocap.files.SceneData;
import net.mt1006.mocap.mocap.files.SceneFiles;
import net.mt1006.mocap.mocap.playing.Playing;
import net.mt1006.mocap.mocap.playing.playback.PlaybackRoot;
import net.mt1006.mocap.mocap.recording.Recording;
import net.mt1006.mocap.mocap.recording.RecordingContext;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CommandSuggestions
{
	private static final int RECORDINGS = 1;
	private static final int SCENES = 2;
	private static final int CURRENTLY_RECORDED = 4;
	private static final int PLAYABLE = RECORDINGS | SCENES | CURRENTLY_RECORDED;

	public static final Set<String> inputSet = new HashSet<>();
	public static final Set<String> skinFileSet = new HashSet<>();
	public static final Map<String, List<String>> sceneElementCache = new HashMap<>();

	private static CompletableFuture<Suggestions> inputSuggestions(SuggestionsBuilder builder, int suggestionFlags, boolean ignoreFirstChar)
	{
		String remaining = builder.getRemaining();
		for (String input : inputSet)
		{
			int type = switch (input.charAt(0))
			{
				case '.' -> SCENES;
				case '-' -> CURRENTLY_RECORDED;
				default -> RECORDINGS;
			};

			if ((suggestionFlags & type) != 0 && (input.startsWith(remaining)
					|| (ignoreFirstChar && input.substring(1).startsWith(remaining))))
			{
				builder.suggest(input);
			}
		}
		return builder.buildFuture();
	}

	public static CompletableFuture<Suggestions> recording(CommandContext<?> ctx, SuggestionsBuilder builder)
	{
		return inputSuggestions(builder, RECORDINGS, false);
	}

	public static CompletableFuture<Suggestions> scene(CommandContext<?> ctx, SuggestionsBuilder builder)
	{
		return inputSuggestions(builder, SCENES, true);
	}

	public static CompletableFuture<Suggestions> currentlyRecorded(CommandContext<?> ctx, SuggestionsBuilder builder)
	{
		return inputSuggestions(builder, CURRENTLY_RECORDED, true);
	}

	public static CompletableFuture<Suggestions> playable(CommandContext<?> ctx, SuggestionsBuilder builder)
	{
		return inputSuggestions(builder, PLAYABLE, false);
	}

	public static CompletableFuture<Suggestions> playbackId(CommandContext<?> ctx, SuggestionsBuilder builder)
	{
		String remaining = builder.getRemaining();
		for (PlaybackRoot playback : Playing.playbacks)
		{
			String str = playback.getSuggestedId();
			if (str.startsWith(remaining)) { builder.suggest(str); }
		}
		return builder.buildFuture();
	}

	public static CompletableFuture<Suggestions> sceneElement(CommandContext<?> ctx, SuggestionsBuilder builder)
	{
		String sceneName = StringArgumentType.getString(ctx, "scene_name");
		if (sceneName.isEmpty()) { return builder.buildFuture(); }
		if (sceneName.charAt(0) != '.') { sceneName = "." + sceneName; }

		if (!inputSet.contains(sceneName)) { return builder.buildFuture(); }
		List<String> elements = sceneElementCache.get(sceneName);

		if (elements == null)
		{
			SceneData sceneData = new SceneData();
			if (!sceneData.load(CommandOutput.LOGS, sceneName)) { builder.buildFuture(); }
			elements = sceneData.saveToSceneElementCache(sceneName);
		}
		if (elements == null) { return builder.buildFuture(); }

		String remaining = builder.getRemaining();
		for (String str : elements)
		{
			if (str.startsWith(remaining)) { builder.suggest(str); }
		}
		return builder.buildFuture();
	}

	public static CompletableFuture<Suggestions> skinFile(CommandContext<?> ctx, SuggestionsBuilder builder)
	{
		String remaining = builder.getRemaining();
		for (String input : skinFileSet)
		{
			if (input.startsWith(remaining)) { builder.suggest(input); }
		}
		return builder.buildFuture();
	}

	public static void refresh()
	{
		initInputSet();
		initSkinSet();
	}

	public static void clearCache()
	{
		sceneElementCache.clear();
	}

	private static void initInputSet()
	{
		inputSet.clear();

		List<String> recordingList = RecordingFiles.list();
		if (recordingList != null) { inputSet.addAll(recordingList); }

		List<String> sceneList = SceneFiles.list();
		if (sceneList != null) { inputSet.addAll(sceneList); }

		Recording.allContexts().forEach((ctx) -> inputSet.add(ctx.id.str));
	}

	private static void initSkinSet()
	{
		skinFileSet.clear();
		if (!Files.initialized) { return; }

		String[] skinList = Files.skinDirectory.list(Files::isSkinFile);
		String[] slimSkinList = Files.slimSkinDirectory.list(Files::isSkinFile);

		if (skinList != null)
		{
			for (String filename : skinList)
			{
				skinFileSet.add(filename.substring(0, filename.lastIndexOf('.')));
			}
		}
		if (slimSkinList != null)
		{
			for (String filename : slimSkinList)
			{
				skinFileSet.add("slim/" + filename.substring(0, filename.lastIndexOf('.')));
			}
		}
	}
}
