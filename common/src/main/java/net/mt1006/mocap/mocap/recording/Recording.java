package net.mt1006.mocap.mocap.recording;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.command.CommandSuggestions;
import net.mt1006.mocap.command.CommandsContext;
import net.mt1006.mocap.command.io.CommandInfo;
import net.mt1006.mocap.command.io.CommandOutput;
import net.mt1006.mocap.mocap.files.Files;
import net.mt1006.mocap.mocap.files.RecordingFiles;
import net.mt1006.mocap.mocap.playing.Playing;
import net.mt1006.mocap.mocap.settings.Settings;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class Recording
{
	//TODO: add second "recording stop" required after stopped by death
	//TODO: temporary enum and variable, move it to config
	private enum QuickDiscard
	{
		ALLOW,
		DISALLOW,
		SAFE;

		public boolean allowsSingle(RecordingContext ctx)
		{
			//TODO: test if ends with death
			return this == SAFE || this == ALLOW;
		}

		public boolean canBeUsed(RecordingContext ctx, @Nullable ServerPlayer source)
		{
			return allowsSingle(ctx) && source != null && bySourcePlayer(source).size() == 1;
		}
	}

	private static final QuickDiscard quickDiscard = QuickDiscard.SAFE;

	private static final Multimap<String, RecordingContext> contextsBySource = HashMultimap.create();
	private static final Collection<RecordingContext> contexts = contextsBySource.values();
	public static final BiMultimap<ServerPlayer, RecordingContext> waitingForRespawn = new BiMultimap<>();

	public static @Nullable RecordingContext startOrWait(CommandInfo commandInfo, ServerPlayer player,
														 RecordingSource source, @Nullable String instantSave)
	{
		if (!checkDoubleStart(commandInfo, player)) { return null; }
		boolean startInstantly = Settings.START_INSTANTLY.val;

		RecordingContext ctx = start(player, source, instantSave, startInstantly, true);
		if (ctx != null && !startInstantly)
		{
			commandInfo.sendSuccess(player.equals(commandInfo.getSourcePlayer())
					? "recording.start.waiting_for_action.self"
					: "recording.start.waiting_for_action.another_player");
		}
		if (ctx == null)
		{
			commandInfo.sendFailure("recording.start.error");
		}
		return ctx;
	}

	public static @Nullable RecordingContext start(ServerPlayer player, RecordingSource source,
												   @Nullable String instantSave, boolean startNow, boolean sendMessage)
	{
		RecordingId id = new RecordingId(contexts, player, source.name);
		if (!id.isProper()) { return null; }

		RecordingContext ctx = new RecordingContext(id, player, source, instantSave);
		contextsBySource.put(source.name, ctx);
		CommandSuggestions.inputSet.add(id.str);

		if (startNow) { ctx.start(sendMessage); }
		return ctx;
	}

	private static boolean checkDoubleStart(CommandInfo commandInfo, ServerPlayer recordedPlayer)
	{
		ServerPlayer sourcePlayer = commandInfo.getSourcePlayer();
		if (sourcePlayer == null) { return true; }

		String recordedPlayerName = recordedPlayer.getName().getString();

		if (!recordedPlayerName.equals(CommandsContext.get(sourcePlayer).doubleStart))
		{
			for (RecordingContext ctx : contexts)
			{
				if (ctx.source.player == sourcePlayer && ctx.recordedPlayer == recordedPlayer)
				{
					handleDoubleStart(commandInfo, ctx);
					return false;
				}
			}
		}
		CommandsContext.get(sourcePlayer).doubleStart = null;
		return true;
	}

	private static void handleDoubleStart(CommandOutput commandOutput, RecordingContext ctx)
	{
		boolean addToDoubleStart = false;

		switch (ctx.state)
		{
			case WAITING_FOR_ACTION:
				ctx.start(true);
				break;

			case RECORDING:
				commandOutput.sendFailureWithTip("recording.start.already_recording");
				addToDoubleStart = true;
				break;

			case WAITING_FOR_DECISION:
				commandOutput.sendFailureWithTip("recording.start.waiting_for_decision");
				addToDoubleStart = true;
				break;

			default:
				MocapMod.LOGGER.error("Undefined recording context state supplied to double start handler!");
				commandOutput.sendFailureWithTip("recording.start.error");
				break;
		}

		if (addToDoubleStart && ctx.source.player != null)
		{
			CommandsContext.get(ctx.source.player).doubleStart = ctx.recordedPlayer.getName().getString();
		}
	}

	public static boolean startMultiple(CommandInfo commandInfo, String str)
	{
		//TODO: replace CommandInfo with CommandOutput?
		//TODO: finish
		return true;
	}

	public static boolean stop(CommandInfo commandInfo, @Nullable String id)
	{
		ResolvedContexts resolvedContexts = ResolvedContexts.resolve(commandInfo, id, false);
		if (resolvedContexts == null) { return false; }

		boolean success = resolvedContexts.isSingle
				? stopSingle(commandInfo, resolvedContexts.list.iterator().next())
				: stopMultiple(commandInfo, resolvedContexts.list);

		if (CommandsContext.haveSyncEnabled != 0) { refreshSyncOnStop(resolvedContexts); }
		return success;
	}

	public static boolean stopSingle(CommandInfo commandInfo, RecordingContext ctx)
	{
		if (ctx.state == RecordingContext.State.WAITING_FOR_DECISION)
		{
			if (!quickDiscard.allowsSingle(ctx))
			{
				//TODO: proper message when blocked by death
				commandInfo.sendFailureWithTip("recording.stop.quick_discard.disabled");
				return false;
			}
			return discardSingle(commandInfo, ctx);
		}

		ctx.stop(commandInfo);

		switch (ctx.state)
		{
			case WAITING_FOR_DECISION:
				commandInfo.sendSuccess("recording.stop.stopped");
				if (Settings.SHOW_TIPS.val)
				{
					commandInfo.sendSuccess(quickDiscard.canBeUsed(ctx, commandInfo.getSourcePlayer())
							? "recording.stop.stopped.stop_tip"
							: "recording.stop.stopped.discard_tip");
				}
				return true;

			case CANCELED:
				commandInfo.sendSuccess("recording.stop.canceled");
				return true;

			case SAVED:
				commandInfo.sendSuccess("recording.stop.instant_save", ctx.instantSave != null ? ctx.instantSave : "[error]");
				return true;

			default:
				commandInfo.sendFailure("recording.undefined_state", ctx.state.name());
				return false;
		}
	}

	private static boolean stopMultiple(CommandOutput commandOutput, Collection<RecordingContext> contexts)
	{
		int stopped = 0, cancelled = 0, saved = 0, stillWaiting = 0, unknownState = 0;

		for (RecordingContext ctx : contexts)
		{
			if (ctx.state == RecordingContext.State.WAITING_FOR_DECISION)
			{
				stillWaiting++;
				continue;
			}

			ctx.stop(commandOutput);

			switch (ctx.state)
			{
				case WAITING_FOR_DECISION: stopped++; break;
				case CANCELED: cancelled++; break;
				case SAVED: saved++; break;
				default: unknownState++;
			}
		}

		if (stopped == 0 && cancelled == 0 && saved == 0 && stillWaiting == 0 && unknownState == 0)
		{
			commandOutput.sendSuccess("recording.multiple.results.none");
			return true;
		}

		commandOutput.sendSuccess("recording.multiple.results");
		if (stopped != 0) { commandOutput.sendSuccess("recording.multiple.results.stopped", stopped); }
		if (cancelled != 0) { commandOutput.sendSuccess("recording.multiple.results.cancelled", cancelled); }
		if (saved != 0) { commandOutput.sendSuccess("recording.multiple.results.saved", saved); }
		if (stillWaiting != 0) { commandOutput.sendSuccess("recording.multiple.results.still_waiting", stillWaiting); }

		if (unknownState != 0)
		{
			commandOutput.sendSuccess("recording.multiple.results.error", unknownState);
			commandOutput.sendFailure("recording.multiple.undefined_state");
			return false;
		}
		return true;
	}

	public static @Nullable RecordingContext resolveSingle(CommandInfo commandInfo, String id)
	{
		ResolvedContexts resolvedContexts = ResolvedContexts.resolve(commandInfo, id, false);
		return (resolvedContexts != null && resolvedContexts.isSingle) ? resolvedContexts.list.iterator().next() : null;
	}

	private static void refreshSyncOnStop(ResolvedContexts resolvedContexts)
	{
		Set<ServerPlayer> players = new HashSet<>();
		for (RecordingContext ctx : resolvedContexts.list)
		{
			if (ctx.source.player != null) { players.add(ctx.source.player); }
		}

		for (ServerPlayer player : players)
		{
			boolean stillRecording = false;
			for (RecordingContext ctx : bySourcePlayer(player))
			{
				if (ctx.state == RecordingContext.State.RECORDING)
				{
					stillRecording = true;
					break;
				}
			}

			if (!stillRecording) { Playing.stopAll(CommandOutput.DUMMY, player); }
		}
	}

	public static boolean discard(CommandInfo commandInfo, @Nullable String id)
	{
		ResolvedContexts resolvedContexts = ResolvedContexts.resolve(commandInfo, id, false);
		if (resolvedContexts == null) { return false; }

		if (resolvedContexts.isSingle)
		{
			RecordingContext ctx = resolvedContexts.list.iterator().next();
			boolean showQuickDiscardTip = quickDiscard.canBeUsed(ctx, commandInfo.getSourcePlayer()) && Settings.SHOW_TIPS.val;
			boolean success = discardSingle(commandInfo, ctx);

			if (success && ctx.state == RecordingContext.State.DISCARDED && showQuickDiscardTip)
			{
				commandInfo.sendSuccess("recording.discard.quick_discard_tip");
			}
			return success;
		}
		else
		{
			return discardMultiple(commandInfo, resolvedContexts.list);
		}
	}

	public static boolean discardSingle(CommandOutput commandOutput, RecordingContext ctx)
	{
		if (ctx.state == RecordingContext.State.RECORDING)
		{
			commandOutput.sendFailure("recording.discard.not_stopped");
			return false;
		}

		ctx.discard();

		switch (ctx.state)
		{
			case DISCARDED:
				commandOutput.sendSuccess("recording.discard.discarded");
				return true;

			case CANCELED:
				commandOutput.sendSuccess("recording.stop.canceled");
				return true;

			default:
				commandOutput.sendFailure("recording.undefined_state", ctx.state.name());
				return false;
		}
	}

	private static boolean discardMultiple(CommandOutput commandOutput, Collection<RecordingContext> contexts)
	{
		int discarded = 0, cancelled = 0, stillRecording = 0, unknownState = 0;

		for (RecordingContext ctx : contexts)
		{
			if (ctx.state == RecordingContext.State.RECORDING)
			{
				stillRecording++;
				continue;
			}

			ctx.discard();

			switch (ctx.state)
			{
				case DISCARDED: discarded++; break;
				case CANCELED: cancelled++; break;
				default: unknownState++;
			}
		}

		if (discarded == 0 && cancelled == 0 && stillRecording == 0 && unknownState == 0)
		{
			commandOutput.sendSuccess("recording.multiple.results.none");
			return true;
		}

		commandOutput.sendSuccess("recording.multiple.results");
		if (discarded != 0) { commandOutput.sendSuccess("recording.multiple.results.discarded", discarded); }
		if (cancelled != 0) { commandOutput.sendSuccess("recording.multiple.results.cancelled", cancelled); }
		if (stillRecording != 0) { commandOutput.sendSuccess("recording.multiple.results.still_recording", stillRecording); }

		if (unknownState != 0)
		{
			commandOutput.sendSuccess("recording.multiple.results.error", unknownState);
			commandOutput.sendFailure("recording.multiple.undefined_state");
			return false;
		}
		return true;
	}

	public static boolean save(CommandInfo commandInfo, @Nullable String id, String name)
	{
		ResolvedContexts resolvedContexts = ResolvedContexts.resolve(commandInfo, id, false);
		if (resolvedContexts == null) { return false; }

		return resolvedContexts.isSingle
				? saveSingle(commandInfo, resolvedContexts.list.iterator().next(), name, true)
				: saveMultiple(commandInfo, resolvedContexts.list, name);
	}

	public static boolean saveSingle(CommandOutput commandOutput, RecordingContext ctx, String name, boolean sendSavedMessage)
	{
		if (ctx.state == RecordingContext.State.RECORDING)
		{
			commandOutput.sendFailure("recording.save.not_stopped");
			return false;
		}

		File recordingFile = Files.getRecordingFile(commandOutput, name);
		if (recordingFile == null) { return false; }

		if (recordingFile.exists())
		{
			commandOutput.sendFailure("recording.save.already_exists");

			String alternativeName = RecordingFiles.findAlternativeName(name);
			if (alternativeName != null)
			{
				commandOutput.sendFailure("recording.save.already_exists.alternative", alternativeName);
			}
			return false;
		}

		ctx.save(recordingFile, name);

		switch (ctx.state)
		{
			case SAVED:
				if (sendSavedMessage) { commandOutput.sendSuccess("recording.save.saved"); }
				return true;

			case WAITING_FOR_DECISION:
				commandOutput.sendFailure("recording.save.error");
				return false;

			default:
				commandOutput.sendFailure("recording.undefined_state", ctx.state.name());
				return false;
		}
	}

	private static boolean saveMultiple(CommandOutput commandOutput, Collection<RecordingContext> contexts, String namePrefix)
	{
		List<RecordingContext> stopped = new ArrayList<>();
		for (RecordingContext ctx : contexts)
		{
			if (ctx.state == RecordingContext.State.WAITING_FOR_DECISION) { stopped.add(ctx); }
		}

		if (stopped.isEmpty())
		{
			commandOutput.sendFailure("recording.save.multiple.nothing_to_save");
			return false;
		}

		List<String> filenames = new ArrayList<>();
		List<File> files = new ArrayList<>();
		for (int i = 1; i <= stopped.size(); i++)
		{
			String filename = String.format("%s%d", namePrefix, i);
			File recordingFile = Files.getRecordingFile(commandOutput, filename);
			if (recordingFile == null) { return false; }

			if (recordingFile.exists())
			{
				commandOutput.sendFailure("recording.save.multiple.already_exists", filename);
				return false;
			}

			filenames.add(filename);
			files.add(recordingFile);
		}

		boolean somethingFailed = false;
		for (int i = 0; i < stopped.size(); i++)
		{
			RecordingContext ctx = stopped.get(i);
			ctx.save(files.get(i), filenames.get(i));

			switch (ctx.state)
			{
				case SAVED -> commandOutput.sendSuccess("recording.save.multiple.saved", ctx.id.str, filenames.get(i));
				case WAITING_FOR_DECISION -> commandOutput.sendFailure("recording.save.multiple.failed", ctx.id.str, filenames.get(i));
				default -> commandOutput.sendFailure("recording.save.multiple.unknown_state", ctx.id.str, filenames.get(i), ctx.state.name());
			}
			if (ctx.state != RecordingContext.State.SAVED) { somethingFailed = true; }
		}

		if (somethingFailed) { commandOutput.sendFailure("recording.save.multiple.error"); }
		return !somethingFailed;
	}

	public static boolean list(CommandInfo commandInfo, @Nullable String id)
	{
		ResolvedContexts resolvedContexts = ResolvedContexts.resolve(commandInfo, id, true);
		if (resolvedContexts == null) { return false; }

		if (resolvedContexts.isSingle)
		{
			RecordingContext ctx = resolvedContexts.list.iterator().next();
			commandInfo.sendSuccess("recording.list.state", resolvedContexts.fullId.str, ctx.state.name());
			return true;
		}

		ArrayList<String> waitingForAction = new ArrayList<>(), recording = new ArrayList<>(),
				waitingForDecision = new ArrayList<>(), error = new ArrayList<>();

		for (RecordingContext ctx : resolvedContexts.list)
		{
			ArrayList<String> list = switch (ctx.state)
			{
				case WAITING_FOR_ACTION -> waitingForAction;
				case RECORDING -> recording;
				case WAITING_FOR_DECISION -> waitingForDecision;
				default -> error;
			};

			list.add(ctx.id.str);
		}

		Collections.sort(waitingForAction);
		Collections.sort(recording);
		Collections.sort(waitingForDecision);
		Collections.sort(error);

		commandInfo.sendSuccess("recording.list.list", resolvedContexts.fullId.str);

		if (waitingForAction.isEmpty()) { commandInfo.sendSuccess("recording.list.waiting_for_action.none"); }
		else { commandInfo.sendSuccess("recording.list.waiting_for_action", StringUtils.join(waitingForAction, " ")); }

		if (recording.isEmpty()) { commandInfo.sendSuccess("recording.list.recording.none"); }
		else { commandInfo.sendSuccess("recording.list.recording", StringUtils.join(recording, " ")); }

		if (waitingForDecision.isEmpty()) { commandInfo.sendSuccess("recording.list.waiting_for_decision.none"); }
		else { commandInfo.sendSuccess("recording.list.waiting_for_decision", StringUtils.join(waitingForDecision, " ")); }

		if (!error.isEmpty())
		{
			commandInfo.sendSuccess("recording.list.unknown_state", StringUtils.join(error, " "));
			commandInfo.sendSuccess("recording.list.unknown_state.error");
		}
		return true;
	}

	public static void onTick()
	{
		contexts.forEach(RecordingContext::onTick);
	}

	public static boolean isRecordedPlayer(@Nullable Entity entity)
	{
		if (contexts.isEmpty() || !(entity instanceof ServerPlayer)) { return false; }

		for (RecordingContext ctx : contexts)
		{
			if (entity.equals(ctx.recordedPlayer)) { return true; }
		}
		return false;
	}

	public static boolean isActive()
	{
		return !contexts.isEmpty();
	}

	public static List<RecordingContext> byRecordedPlayer(int id)
	{
		List<RecordingContext> list = new ArrayList<>(1);
		for (RecordingContext ctx : contexts)
		{
			if (ctx.recordedPlayer.getId() == id) { list.add(ctx); }
		}
		return list;
	}

	public static List<RecordingContext> byRecordedPlayer(Entity entity)
	{
		if (!(entity instanceof Player)) { return List.of(); }

		List<RecordingContext> list = new ArrayList<>(1);
		for (RecordingContext ctx : contexts)
		{
			if (ctx.recordedPlayer == entity) { list.add(ctx); }
		}
		return list;
	}

	public static @Nullable Collection<RecordingContext> resolveContexts(CommandInfo commandInfo, String id)
	{
		ResolvedContexts resolvedContexts = ResolvedContexts.resolve(commandInfo, id, false);
		return resolvedContexts != null ? resolvedContexts.list : null;
	}

	public static List<EntityTracker.TrackedEntity> listTrackedEntities(Entity entity)
	{
		List<EntityTracker.TrackedEntity> list = new ArrayList<>(1);
		for (RecordingContext ctx : contexts)
		{
			EntityTracker.TrackedEntity trackedEntity = ctx.getTrackedEntity(entity);
			if (trackedEntity != null) { list.add(trackedEntity); }
		}
		return list;
	}

	public static Collection<RecordingContext> bySourcePlayer(ServerPlayer player)
	{
		return contextsBySource.get(player.getName().getString());
	}

	public static void removeContext(RecordingContext ctx)
	{
		waitingForRespawn.removeByValue(ctx);
		contextsBySource.remove(ctx.source.name, ctx);
		CommandSuggestions.inputSet.remove(ctx.id.str);
	}

	public static Collection<RecordingContext> allContexts()
	{
		return contexts;
	}

	public static void onServerStop()
	{
		waitingForRespawn.clear();
		contextsBySource.clear();
	}

	public static class BiMultimap<A, B>
	{
		public final Multimap<A, B> byKey = HashMultimap.create();
		public final Multimap<B, A> byValue = HashMultimap.create();

		public void put(A key, B val)
		{
			byKey.put(key, val);
			byValue.put(val, key);
		}

		public void removeByKey(A key)
		{
			Collection<B> values = byKey.removeAll(key);
			values.forEach((v) -> byValue.remove(v, key));
		}

		public void removeByValue(B val)
		{
			Collection<A> keys = byValue.removeAll(val);
			keys.forEach((k) -> byKey.remove(k, val));
		}

		public void clear()
		{
			byKey.clear();
			byValue.clear();
		}

		public boolean isEmpty()
		{
			return byKey.isEmpty() && byValue.isEmpty();
		}
	}

	private record ResolvedContexts(Collection<RecordingContext> list, boolean isSingle, RecordingId fullId)
	{
		public static @Nullable ResolvedContexts resolve(CommandInfo commandInfo, @Nullable String idStr, boolean listMode)
		{
			//TODO: test when empty
			if (idStr == null)
			{
				if (listMode) { return new ResolvedContexts(contexts, false, RecordingId.ALL); }
				RecordingContext ctx = resolveEmpty(commandInfo);
				return ctx != null ? new ResolvedContexts(List.of(ctx), true, ctx.id) : null;
			}

			RecordingId id = new RecordingId(idStr, commandInfo.getSourceName());

			if (!id.isProper())
			{
				if (idStr.contains("_"))
				{
					commandInfo.sendFailureWithTip("recording.resolve.improper_group_structure");
				}
				else
				{
					commandInfo.sendFailure("recording.resolve.improper_structure");
					if (!listMode && Settings.SHOW_TIPS.val) { commandInfo.sendFailure("recording.resolve.list_tip"); }
				}
				return null;
			}

			List<RecordingContext> matchingContexts = new ArrayList<>();
			for (RecordingContext ctx : contexts)
			{
				if (ctx.id.matches(id)) { matchingContexts.add(ctx); }
			}

			boolean isSingle = id.isSingle();
			if (isSingle)
			{
				//TODO: improve unexpected error info
				if (matchingContexts.size() > 1) { MocapMod.LOGGER.error("Multiple recording contexts are matching single id!"); }
				if (matchingContexts.isEmpty())
				{
					commandInfo.sendFailure("recording.resolve.not_found");
					if (!listMode && Settings.SHOW_TIPS.val) { commandInfo.sendFailure("recording.resolve.list_tip"); }
					return null;
				}
			}

			return new ResolvedContexts(matchingContexts, isSingle, id);
		}

		private static @Nullable RecordingContext resolveEmpty(CommandInfo commandInfo)
		{
			ServerPlayer source = commandInfo.getSourcePlayer();
			if (source == null)
			{
				commandInfo.sendFailure("failure.resolve_player");
				return null;
			}

			Collection<RecordingContext> sourceContexts = bySourcePlayer(source);

			if (sourceContexts.size() > 1)
			{
				commandInfo.sendFailureWithTip("recording.resolve.multiple_recordings");
				return null;
			}
			else if (sourceContexts.isEmpty())
			{
				if (contexts.isEmpty()) { commandInfo.sendFailure("recording.resolve.server_not_recording"); }
				else { commandInfo.sendFailureWithTip("recording.resolve.player_not_recording"); }
				return null;
			}

			return sourceContexts.iterator().next();
		}
	}
}
