package com.mt1006.mocap.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Pair;
import com.mt1006.mocap.events.PlayerConnectionEvent;
import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.files.SceneFiles;
import com.mt1006.mocap.network.MocapPacketS2C;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class InputArgument
{
	private static final int RECORDINGS = 1;
	private static final int SCENES = 2;
	private static final int CURRENTLY_RECORDED = 4;
	private static final int PLAYABLE = RECORDINGS | SCENES | CURRENTLY_RECORDED;

	public static final HashSet<String> serverInputSet = new HashSet<>();
	public static final HashSet<String> clientInputSet = new HashSet<>();

	public static @Nullable CompletableFuture<Suggestions> getSuggestions(CommandContextBuilder<?> rootCtx, String fullCommand, int cursor)
	{
		CommandContextBuilder<?> ctx = CommandUtils.getFinalCommandContext(rootCtx);
		if (ctx == null) { return null; }

		String subcommand1 = CommandUtils.getNode(ctx, 1);
		String subcommand2 = CommandUtils.getNode(ctx, 2);
		if (subcommand1 == null || subcommand2 == null) { return null; }

		String subcommand = String.format("%s/%s", subcommand1, subcommand2);

		List<Pair<Integer, Integer>> args = ImmutableList.of();

		switch (subcommand)
		{
			case "playing/start":
				args = ImmutableList.of(new Pair<>(3, PLAYABLE));
				break;

			case "recordings/copy":
			case "recordings/rename":
			case "recordings/remove":
			case "recordings/info":
				args = ImmutableList.of(new Pair<>(3, RECORDINGS));
				break;

			case "scenes/copy":
			case "scenes/rename":
			case "scenes/remove":
			case "scenes/removeFrom":
			case "scenes/elementInfo":
			case "scenes/listElements":
			case "scenes/info":
				args = ImmutableList.of(new Pair<>(3, SCENES));
				break;

			case "scenes/addTo":
				args = ImmutableList.of(new Pair<>(3, SCENES), new Pair<>(4, PLAYABLE));
				break;
		}

		if (subcommand.equals("scenes/modify"))
		{
			String paramToModify = CommandUtils.getNode(ctx, 5);
			if (paramToModify == null || !paramToModify.equals("subsceneName")) { args = ImmutableList.of(new Pair<>(3, SCENES)); }
			else { args = ImmutableList.of(new Pair<>(3, SCENES), new Pair<>(6, PLAYABLE)); }
		}

		int suggestionFlags = 0;
		int suggestionPos = 0;
		String prefix = "";

		for (Pair<Integer, Integer> arg : args)
		{
			StringRange stringRange = getStringRange(ctx, arg.getFirst());

			if (stringRange == null)
			{
				StringRange previousStringRange = getStringRange(ctx, arg.getFirst() - 1);
				if (previousStringRange == null || cursor <= previousStringRange.getEnd()) { continue; }

				suggestionFlags = arg.getSecond();
				suggestionPos = cursor;
				prefix = "";
				break;
			}
			else if (cursor >= stringRange.getStart() && cursor <= stringRange.getEnd())
			{
				suggestionFlags = arg.getSecond();
				suggestionPos = stringRange.getStart();
				prefix = stringRange.get(fullCommand);
				break;
			}
		}

		if (suggestionFlags == 0) { return null; }
		SuggestionsBuilder builder = new SuggestionsBuilder(fullCommand, suggestionPos);

		for (String input : clientInputSet)
		{
			int type;
			switch (input.charAt(0))
			{
				case '.': type = SCENES; break;
				case '#': type = CURRENTLY_RECORDED; break;
				default: type = RECORDINGS; break;
			};
			if ((suggestionFlags & type) != 0 && input.startsWith(prefix)) { builder.suggest(input); }
		}
		return builder.buildFuture();
	}

	public static void initServerInputSet(MinecraftServer server)
	{
		serverInputSet.clear();

		List<String> recordingList = RecordingFiles.list(server, CommandOutput.DUMMY);
		if (recordingList != null) { serverInputSet.addAll(recordingList); }

		List<String> sceneList = SceneFiles.list(server, CommandOutput.DUMMY);
		if (sceneList != null) { serverInputSet.addAll(sceneList); }
	}

	public static void addServerInput(String name)
	{
		serverInputSet.add(name);
		PlayerConnectionEvent.players.forEach((player) -> MocapPacketS2C.sendInputSuggestionsAdd(player, ImmutableList.of(name)));
	}

	public static void removeServerInput(String name)
	{
		serverInputSet.remove(name);
		PlayerConnectionEvent.players.forEach((player) -> MocapPacketS2C.sendInputSuggestionsRemove(player, ImmutableList.of(name)));
	}

	private static @Nullable StringRange getStringRange(CommandContextBuilder<?> ctx, int pos)
	{
		if (ctx.getNodes().size() <= pos || pos < 0) { return null; }
		return ctx.getNodes().get(pos).getRange();
	}
}
