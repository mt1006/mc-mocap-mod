package net.mt1006.mocap.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.api.v1.modifiers.MocapEntityFilterBuilder;
import net.mt1006.mocap.mocap.files.Files;
import net.mt1006.mocap.mocap.files.RecordingFiles;
import net.mt1006.mocap.mocap.files.SceneData;
import net.mt1006.mocap.mocap.files.SceneFiles;
import net.mt1006.mocap.mocap.playing.PlaybackManager;
import net.mt1006.mocap.mocap.playing.playable.SceneFile;
import net.mt1006.mocap.mocap.playing.playback.PlaybackRoot;
import net.mt1006.mocap.mocap.recording.RecordingManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class CommandSuggestions
{
	private static final int RECORDINGS = 1;
	private static final int SCENES = 2;
	private static final int CURRENTLY_RECORDED = 4;
	private static final int VIRTUAL = 8;
	private static final int PLAYABLE = RECORDINGS | SCENES | CURRENTLY_RECORDED | VIRTUAL;

	public static final Set<String> inputSet = new HashSet<>();
	public static final Set<String> skinFileSet = new HashSet<>();
	public static final Map<String, List<String>> sceneElementCache = new HashMap<>();

	private static CompletableFuture<Suggestions> inputSuggestions(SuggestionsBuilder builder, int suggestionFlags)
	{
		String remaining = builder.getRemaining();

		if (suggestionFlags == SCENES && !remaining.startsWith(".")) { remaining = "." + remaining; }
		else if (suggestionFlags == CURRENTLY_RECORDED && !remaining.startsWith("-")) { remaining = "-" + remaining; }
		else if (suggestionFlags == VIRTUAL && !remaining.startsWith("+")) { remaining = "+" + remaining; }

		for (String input : inputSet)
		{
			int type = switch (input.charAt(0))
			{
				case '.' -> SCENES;
				case '-' -> CURRENTLY_RECORDED;
				case '+' -> VIRTUAL;
				default -> RECORDINGS;
			};

			if ((suggestionFlags & type) != 0 && input.startsWith(remaining))
			{
				builder.suggest(input);
			}
		}
		return builder.buildFuture();
	}

	public static CompletableFuture<Suggestions> recording(CommandContext<?> ctx, SuggestionsBuilder builder)
	{
		return inputSuggestions(builder, RECORDINGS);
	}

	public static CompletableFuture<Suggestions> scene(CommandContext<?> ctx, SuggestionsBuilder builder)
	{
		return inputSuggestions(builder, SCENES);
	}

	public static CompletableFuture<Suggestions> currentlyRecorded(CommandContext<?> ctx, SuggestionsBuilder builder)
	{
		return inputSuggestions(builder, CURRENTLY_RECORDED);
	}

	public static CompletableFuture<Suggestions> playable(CommandContext<?> ctx, SuggestionsBuilder builder)
	{
		return inputSuggestions(builder, PLAYABLE);
	}

	public static CompletableFuture<Suggestions> playbackId(CommandContext<?> ctx, SuggestionsBuilder builder)
	{
		String remaining = builder.getRemaining();
		for (PlaybackRoot playback : PlaybackManager.playbacks)
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
			if (!sceneData.load(CommandOutput.LOGS, SceneFile.get(CommandOutput.LOGS, sceneName))) { builder.buildFuture(); }
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

	public static CompletableFuture<Suggestions> entityFilter(CommandContext<?> ctx, SuggestionsBuilder builder)
	{
		String remaining = builder.getRemaining();
		int entryStart = remaining.lastIndexOf(';') + 1;
		String entry = remaining.substring(entryStart);

		boolean startsWithMinus = entry.startsWith("-");
		if (startsWithMinus)
		{
			entry = entry.substring(1);
			entryStart++;
		}

		builder = builder.createOffset(builder.getStart() + entryStart);
		List<String> list = new ArrayList<>();

		for (MocapEntityFilterBuilder.Group group : MocapEntityFilterBuilder.Group.values())
		{
			list.add("@" + group.name().toLowerCase());
		}

		Set<String> namespaces = new HashSet<>();
		for (ResourceLocation id : BuiltInRegistries.ENTITY_TYPE.keySet())
		{
			list.add(id.toString());
			namespaces.add(id.getNamespace());
		}
		namespaces.forEach((n) -> list.add(n + ":*"));
		list.add("*");

		String withMcNamespace = "minecraft:" + entry;
		for (String str : list)
		{
			if (str.startsWith(entry) || str.startsWith(withMcNamespace))
			{
				builder.suggest(str);
				if (str.equals(entry))
				{
					builder = builder.createOffset(builder.getStart() + str.length());
					builder.suggest(";");
					return builder.buildFuture();
				}
			}
		}

		if (entry.isEmpty())
		{
			if (!startsWithMinus) { builder.suggest("-"); }
			builder.suggest("$");
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

		RecordingManager.allContexts().forEach((ctx) -> inputSet.add(ctx.id.str));
	}

	private static void initSkinSet()
	{
		skinFileSet.clear();
		if (!Files.initialized) { return; }

		String[] skins = Files.skinDirectory.list(Files::isSkinFile);
		String[] slimSkins = Files.slimSkinDirectory.list(Files::isSkinFile);
		String[] skinLists = Files.skinListDirectory.list(Files::isSkinListFile);

		if (skins != null) { Stream.of(skins).forEach((f) -> skinFileSet.add(f.substring(0, f.lastIndexOf('.')))); }
		if (slimSkins != null) { Stream.of(slimSkins).forEach((f) -> skinFileSet.add("slim/" + f.substring(0, f.lastIndexOf('.')))); }
		if (skinLists != null) { Stream.of(skinLists).forEach((f) -> skinFileSet.add("list/" + f.substring(0, f.lastIndexOf('.')))); }
	}
}
