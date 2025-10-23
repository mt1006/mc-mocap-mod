package net.mt1006.mocap.mocap.recording;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.v1.controller.config.MocapRecordingConfig;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.command.CommandSuggestions;
import net.mt1006.mocap.command.CommandsContext;
import net.mt1006.mocap.mocap.files.RecordingFiles;
import net.mt1006.mocap.mocap.playing.PlaybackManager;
import net.mt1006.mocap.mocap.playing.playable.RecordingFile;
import net.mt1006.mocap.mocap.settings.Settings;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class RecordingManager
{
	private static final Multimap<String, RecordingContext> contextsBySource = HashMultimap.create();
	private static final Collection<RecordingContext> contexts = contextsBySource.values();
	public static final BiMultimap<ServerPlayer, RecordingContext> waitingForRespawn = new BiMultimap<>();

	public static @Nullable RecordingContext startOrWait(CommandInfo info, ServerPlayer player, RecordingSource source,
														 @Nullable String instantSave, boolean multiplePlayers)
	{
		if (!checkDoubleStart(info, player)) { return null; }
		boolean startInstantly = Settings.START_INSTANTLY.val || multiplePlayers;

		RecordingContext ctx = start(player, source,
				MocapRecordingConfig.createFromSettings(), instantSave, startInstantly, !multiplePlayers);
		if (ctx != null && !startInstantly)
		{
			info.sendSuccess(player.equals(info.getSourcePlayer())
					? "recording.start.waiting_for_action.self"
					: "recording.start.waiting_for_action.another_player");
		}
		if (ctx == null)
		{
			info.sendFailure("recording.start.error");
		}
		return ctx;
	}

	public static @Nullable RecordingContext start(ServerPlayer player, RecordingSource source, MocapRecordingConfig config,
												   @Nullable String instantSave, boolean startNow, boolean sendMessage)
	{
		RecordingId id = new RecordingId(contexts, player, source.name);
		if (!id.isProper()) { return null; }

		RecordingContext ctx = new RecordingContext(id, player, source, config, instantSave);
		contextsBySource.put(source.name, ctx);
		CommandSuggestions.inputSet.add(id.str);

		if (startNow) { ctx.start(sendMessage); }
		return ctx;
	}

	private static boolean checkDoubleStart(CommandInfo info, ServerPlayer recordedPlayer)
	{
		ServerPlayer sourcePlayer = info.getSourcePlayer();
		if (sourcePlayer == null) { return true; }

		String recordedPlayerName = recordedPlayer.getName().getString();

		if (!recordedPlayerName.equals(CommandsContext.get(sourcePlayer).doubleStart))
		{
			for (RecordingContext ctx : contexts)
			{
				if (ctx.source.player == sourcePlayer && ctx.recordedPlayer == recordedPlayer)
				{
					handleDoubleStart(info, ctx);
					return false;
				}
			}
		}
		CommandsContext.get(sourcePlayer).doubleStart = null;
		return true;
	}

	private static void handleDoubleStart(CommandOutput out, RecordingContext ctx)
	{
		boolean addToDoubleStart = false;

		switch (ctx.state)
		{
			case WAITING_FOR_ACTION:
				ctx.start(true);
				break;

			case RECORDING:
				out.sendFailureWithTip("recording.start.already_recording");
				addToDoubleStart = true;
				break;

			case WAITING_FOR_DECISION:
				out.sendFailureWithTip("recording.start.waiting_for_decision");
				addToDoubleStart = true;
				break;

			default:
				MocapMod.LOGGER.error("Undefined recording context state supplied to double start handler!");
				out.sendFailureWithTip("recording.start.error");
				break;
		}

		if (addToDoubleStart && ctx.source.player != null)
		{
			CommandsContext.get(ctx.source.player).doubleStart = ctx.recordedPlayer.getName().getString();
		}
	}

	public static boolean stop(CommandInfo info, @Nullable String id)
	{
		ResolvedContexts resolvedContexts = ResolvedContexts.resolve(info, id, false);
		if (resolvedContexts == null) { return false; }

		boolean success = resolvedContexts.isSingle
				? stopSingle(info, resolvedContexts.list.iterator().next(), info.getSourcePlayer())
				: stopMultiple(info, resolvedContexts.list);

		if (CommandsContext.haveSyncEnabled != 0) { refreshSyncOnStop(resolvedContexts); }
		return success;
	}

	public static boolean stopSingle(CommandOutput out, RecordingContext ctx, @Nullable ServerPlayer sourcePlayer)
	{
		if (ctx.state == RecordingContext.State.WAITING_FOR_DECISION)
		{
			if (!Settings.QUICK_DISCARD.val) { return out.sendFailureWithTip("recording.stop.quick_discard.disabled"); }

			if (ctx.requiresSafeDiscard)
			{
				ctx.requiresSafeDiscard = false;
				return out.sendFailureWithTip("recording.stop.quick_discard.safe_discard_required");
			}

			return discardSingle(out, ctx);
		}

		ctx.stop(out);

		return switch (ctx.state)
		{
			case WAITING_FOR_DECISION -> sendStopMessage(out::sendSuccess, ctx, sourcePlayer);
			case CANCELED -> out.sendSuccess("recording.stop.canceled");
			case SAVED -> out.sendSuccess("recording.stop.instant_save", ctx.instantSave != null ? ctx.instantSave : "[error]");
			default -> out.sendFailure("recording.undefined_state", ctx.state.name());
		};
	}

	public static boolean sendStopMessage(Consumer<String> successSender, RecordingContext ctx, @Nullable ServerPlayer sourcePlayer)
	{
		successSender.accept("recording.stop.stopped");
		if (Settings.SHOW_TIPS.val)
		{
			successSender.accept(shouldSuggestQuickDiscard(ctx, sourcePlayer)
					? "recording.stop.stopped.stop_tip"
					: "recording.stop.stopped.discard_tip");
		}
		return true;
	}

	public static boolean shouldSuggestQuickDiscard(RecordingContext ctx, @Nullable ServerPlayer source)
	{
		return Settings.QUICK_DISCARD.val && !ctx.requiresSafeDiscard && source != null && bySourcePlayer(source).size() == 1;
	}

	private static boolean stopMultiple(CommandOutput out, Collection<RecordingContext> contexts)
	{
		int stopped = 0, cancelled = 0, saved = 0, stillWaiting = 0, unknownState = 0;

		for (RecordingContext ctx : contexts)
		{
			if (ctx.state == RecordingContext.State.WAITING_FOR_DECISION)
			{
				stillWaiting++;
				continue;
			}

			ctx.stop(out);

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
			out.sendSuccess("recording.multiple.results.none");
			return true;
		}

		out.sendSuccess("recording.multiple.results");
		if (stopped != 0) { out.sendSuccess("recording.multiple.results.stopped", stopped); }
		if (cancelled != 0) { out.sendSuccess("recording.multiple.results.cancelled", cancelled); }
		if (saved != 0) { out.sendSuccess("recording.multiple.results.saved", saved); }
		if (stillWaiting != 0) { out.sendSuccess("recording.multiple.results.still_waiting", stillWaiting); }

		if (unknownState != 0)
		{
			out.sendSuccess("recording.multiple.results.error", unknownState);
			return out.sendFailure("recording.multiple.undefined_state");
		}
		return true;
	}

	public static @Nullable RecordingContext resolveSingle(CommandInfo info, String id)
	{
		ResolvedContexts resolvedContexts = ResolvedContexts.resolve(info, id, false);
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

			if (!stillRecording) { PlaybackManager.stopAll(CommandOutput.DUMMY, player); }
		}
	}

	public static boolean discard(CommandInfo out, @Nullable String id)
	{
		ResolvedContexts resolvedContexts = ResolvedContexts.resolve(out, id, false);
		if (resolvedContexts == null) { return false; }

		if (resolvedContexts.isSingle)
		{
			RecordingContext ctx = resolvedContexts.list.iterator().next();
			boolean showQuickDiscardTip = shouldSuggestQuickDiscard(ctx, out.getSourcePlayer()) && Settings.SHOW_TIPS.val;
			boolean success = discardSingle(out, ctx);

			if (success && ctx.state == RecordingContext.State.DISCARDED && showQuickDiscardTip)
			{
				out.sendSuccess("recording.discard.quick_discard_tip");
			}
			return success;
		}
		else
		{
			return discardMultiple(out, resolvedContexts.list);
		}
	}

	public static boolean discardSingle(CommandOutput out, RecordingContext ctx)
	{
		if (ctx.state == RecordingContext.State.RECORDING)
		{
			out.sendFailure("recording.discard.not_stopped");
			return false;
		}

		ctx.discard();

		return switch (ctx.state)
		{
			case DISCARDED -> out.sendSuccess("recording.discard.discarded");
			case CANCELED -> out.sendSuccess("recording.stop.canceled");
			default -> out.sendFailure("recording.undefined_state", ctx.state.name());
		};
	}

	private static boolean discardMultiple(CommandOutput out, Collection<RecordingContext> contexts)
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
			out.sendSuccess("recording.multiple.results.none");
			return true;
		}

		out.sendSuccess("recording.multiple.results");
		if (discarded != 0) { out.sendSuccess("recording.multiple.results.discarded", discarded); }
		if (cancelled != 0) { out.sendSuccess("recording.multiple.results.cancelled", cancelled); }
		if (stillRecording != 0) { out.sendSuccess("recording.multiple.results.still_recording", stillRecording); }

		if (unknownState != 0)
		{
			out.sendSuccess("recording.multiple.results.error", unknownState);
			return out.sendFailure("recording.multiple.undefined_state");
		}
		return true;
	}

	public static boolean save(CommandInfo info, @Nullable String id, String name)
	{
		ResolvedContexts resolvedContexts = ResolvedContexts.resolve(info, id, false);
		if (resolvedContexts == null) { return false; }

		return resolvedContexts.isSingle
				? saveSingle(info, resolvedContexts.list.iterator().next(), name, true)
				: saveMultiple(info, resolvedContexts.list, name);
	}

	public static boolean saveSingle(CommandOutput out, RecordingContext ctx, String name, boolean sendSavedMessage)
	{
		if (ctx.state == RecordingContext.State.RECORDING) { return out.sendFailure("recording.save.not_stopped"); }

		RecordingFile recordingFile = RecordingFile.get(out, name);
		if (recordingFile == null) { return false; }

		if (recordingFile.exists())
		{
			out.sendFailure("recording.save.already_exists");

			String alternativeName = RecordingFiles.findAlternativeName(name);
			if (alternativeName != null)
			{
				out.sendFailure("recording.save.already_exists.alternative", alternativeName);
			}
			return false;
		}

		ctx.save(recordingFile.getFile(), name);

		switch (ctx.state)
		{
			case SAVED:
				if (sendSavedMessage) { out.sendSuccess("recording.save.saved"); }
				return true;

			case WAITING_FOR_DECISION:
				return out.sendFailure("recording.save.error");

			default:
				return out.sendFailure("recording.undefined_state", ctx.state.name());
		}
	}

	private static boolean saveMultiple(CommandOutput out, Collection<RecordingContext> contexts, String namePrefix)
	{
		List<RecordingContext> stopped = new ArrayList<>();
		for (RecordingContext ctx : contexts)
		{
			if (ctx.state == RecordingContext.State.WAITING_FOR_DECISION) { stopped.add(ctx); }
		}

		if (stopped.isEmpty())
		{
			out.sendFailure("recording.save.multiple.nothing_to_save");
			return false;
		}

		List<String> filenames = new ArrayList<>();
		List<File> files = new ArrayList<>();
		for (int i = 1; i <= stopped.size(); i++)
		{
			String filename = String.format("%s%d", namePrefix, i);
			RecordingFile recordingFile = RecordingFile.get(out, filename);
			if (recordingFile == null) { return false; }

			if (recordingFile.exists())
			{
				out.sendFailure("recording.save.multiple.already_exists", filename);
				return false;
			}

			filenames.add(filename);
			files.add(recordingFile.getFile());
		}

		boolean somethingFailed = false;
		for (int i = 0; i < stopped.size(); i++)
		{
			RecordingContext ctx = stopped.get(i);
			ctx.save(files.get(i), filenames.get(i));

			switch (ctx.state)
			{
				case SAVED -> out.sendSuccess("recording.save.multiple.saved", ctx.id.str, filenames.get(i));
				case WAITING_FOR_DECISION -> out.sendFailure("recording.save.multiple.failed", ctx.id.str, filenames.get(i));
				default -> out.sendFailure("recording.save.multiple.unknown_state", ctx.id.str, filenames.get(i), ctx.state.name());
			}
			if (ctx.state != RecordingContext.State.SAVED) { somethingFailed = true; }
		}

		if (somethingFailed) { out.sendFailure("recording.save.multiple.error"); }
		return !somethingFailed;
	}

	public static boolean list(CommandInfo info, @Nullable String id)
	{
		ResolvedContexts resolvedContexts = ResolvedContexts.resolve(info, id, true);
		if (resolvedContexts == null) { return false; }

		if (resolvedContexts.isSingle)
		{
			RecordingContext ctx = resolvedContexts.list.iterator().next();
			return info.sendSuccess("recording.list.state", resolvedContexts.fullId.str, ctx.state.name());
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

		info.sendSuccess("recording.list.list", resolvedContexts.fullId.str);

		if (waitingForAction.isEmpty()) { info.sendSuccess("recording.list.waiting_for_action.none"); }
		else { info.sendSuccess("recording.list.waiting_for_action", StringUtils.join(waitingForAction, " ")); }

		if (recording.isEmpty()) { info.sendSuccess("recording.list.recording.none"); }
		else { info.sendSuccess("recording.list.recording", StringUtils.join(recording, " ")); }

		if (waitingForDecision.isEmpty()) { info.sendSuccess("recording.list.waiting_for_decision.none"); }
		else { info.sendSuccess("recording.list.waiting_for_decision", StringUtils.join(waitingForDecision, " ")); }

		if (!error.isEmpty())
		{
			info.sendSuccess("recording.list.unknown_state", StringUtils.join(error, " "));
			info.sendSuccess("recording.list.unknown_state.error");
		}
		return true;
	}

	public static void onTick()
	{
		// copies list of contexts to prevent concurrent modification exception,
		// e.g. when splitting recording or when external (API) controllers start/stop recording
		new ArrayList<>(contexts).forEach(RecordingContext::onTick);
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

	public static @Nullable Collection<RecordingContext> resolveContexts(CommandInfo info, String id)
	{
		ResolvedContexts resolvedContexts = ResolvedContexts.resolve(info, id, false);
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
		public static @Nullable ResolvedContexts resolve(CommandInfo info, @Nullable String idStr, boolean listMode)
		{
			//TODO: test when empty
			if (idStr == null)
			{
				if (listMode) { return new ResolvedContexts(contexts, false, RecordingId.ALL); }
				RecordingContext ctx = resolveEmpty(info);
				return ctx != null ? new ResolvedContexts(List.of(ctx), true, ctx.id) : null;
			}

			RecordingId id = new RecordingId(idStr, info.getSourceName());

			if (!id.isProper())
			{
				if (idStr.contains("_"))
				{
					info.sendFailureWithTip("recording.resolve.improper_group_structure");
				}
				else
				{
					info.sendFailure("recording.resolve.improper_structure");
					if (!listMode && Settings.SHOW_TIPS.val) { info.sendFailure("recording.resolve.list_tip"); }
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
					info.sendFailure("recording.resolve.not_found");
					if (!listMode && Settings.SHOW_TIPS.val) { info.sendFailure("recording.resolve.list_tip"); }
					return null;
				}
			}

			return new ResolvedContexts(matchingContexts, isSingle, id);
		}

		private static @Nullable RecordingContext resolveEmpty(CommandInfo info)
		{
			ServerPlayer source = info.getSourcePlayer();
			if (source == null)
			{
				info.sendFailure("failure.resolve_player");
				return null;
			}

			Collection<RecordingContext> sourceContexts = bySourcePlayer(source);

			if (sourceContexts.size() > 1)
			{
				info.sendFailureWithTip("recording.resolve.multiple_recordings");
				return null;
			}
			else if (sourceContexts.isEmpty())
			{
				if (contexts.isEmpty()) { info.sendFailure("recording.resolve.server_not_recording"); }
				else { info.sendFailureWithTip("recording.resolve.player_not_recording"); }
				return null;
			}

			return sourceContexts.iterator().next();
		}
	}
}
