package net.mt1006.mocap.mocap.recording;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.mt1006.mocap.api.v1.controller.config.MocapOnDeath;
import net.mt1006.mocap.api.v1.controller.config.MocapRecordingConfig;
import net.mt1006.mocap.api.v1.extension.MocapActiveRecordingActions;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapBlockAction;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.mocap.actions.*;
import net.mt1006.mocap.mocap.files.RecordingData;
import net.mt1006.mocap.mocap.files.RecordingFiles;
import net.mt1006.mocap.mocap.playing.modifiers.EntityFilter;
import net.mt1006.mocap.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;

public class RecordingContext implements MocapActiveRecordingActions
{
	public final RecordingId id;
	public ServerPlayer recordedPlayer;
	public final RecordingSource source;
	public final MocapRecordingConfig config;
	public final RecordingData data = RecordingData.forWriting();
	public State state = State.WAITING_FOR_ACTION;
	private @Nullable RecordedEntityState entityState = null;
	private final PositionTracker positionTracker;
	private final EntityTracker entityTracker = new EntityTracker(this);
	public final EntityFilter entityFilter;
	public final @Nullable String instantSave;
	private int tick = 0, diedOnTick = 0;
	private boolean died = false;
	private ResourceKey<Level> lastDimension;
	public boolean requiresSafeDiscard = false;

	public RecordingContext(RecordingId id, ServerPlayer recordedPlayer, RecordingSource source,
							MocapRecordingConfig config, @Nullable String instantSave)
	{
		this.id = id;
		this.recordedPlayer = recordedPlayer;
		this.source = source;
		this.config = config;
		this.positionTracker = new PositionTracker(recordedPlayer, false, recordedPlayer.position());
		this.entityFilter = EntityFilter.FOR_RECORDING;
		this.instantSave = instantSave;
		this.lastDimension = recordedPlayer.level().dimension();

		this.positionTracker.writeStartPos(data);

		data.assignedProfile = RecordingData.AssignedProfile.create(recordedPlayer.getGameProfile(), config.getAssignProfile());
		if (config.getAssignDimension()) { data.dimensionId = recordedPlayer.level().dimension().location(); }
	}

	public void start(boolean sendMessage)
	{
		entityState = null;
		state = State.RECORDING;
		if (sendMessage) { Utils.sendMessage(source.player, "recording.start.recording_started"); }
	}

	public void stop(CommandOutput out)
	{
		state = switch (state)
		{
			case WAITING_FOR_ACTION -> State.CANCELED;
			case RECORDING, WAITING_FOR_DECISION -> State.WAITING_FOR_DECISION;
			default -> State.UNDEFINED;
		};

		if (state == State.WAITING_FOR_DECISION && instantSave != null)
		{
			RecordingManager.saveSingle(out, this, instantSave, false);
		}

		if (state.removed) { RecordingManager.removeContext(this); }
	}

	public void discard()
	{
		state = switch (state)
		{
			case WAITING_FOR_ACTION -> State.CANCELED;
			case WAITING_FOR_DECISION -> State.DISCARDED;
			default -> State.UNDEFINED;
		};

		if (state.removed) { RecordingManager.removeContext(this); }
	}

	public void save(File recordingFile, String name)
	{
		if (state == State.WAITING_FOR_DECISION)
		{
			if (!RecordingFiles.save(CommandOutput.LOGS, recordingFile, name, data)) { return; }
			state = State.SAVED;
		}
		else
		{
			state = State.UNDEFINED;
		}

		if (state.removed) { RecordingManager.removeContext(this); }
	}

	public void onTick()
	{
		switch (state)
		{
			case WAITING_FOR_ACTION -> onTickWaiting();
			case RECORDING -> onTickRecording();
		}
	}

	private void onTickWaiting()
	{
		RecordedEntityState newEntityState = new RecordedEntityState(recordedPlayer);

		if (newEntityState.differs(entityState) || positionTracker.getDelta() != null) { start(true); }
		else { entityState = newEntityState; }
	}

	//TODO: safe saving
	private void onTickRecording()
	{
		tick++;
		if (died)
		{
			int tickDiff = tick - diedOnTick;
			if (config.getOnDeath() == MocapOnDeath.CONTINUE_SYNCED || tickDiff < 20)
			{
				entityTracker.onTick();
				addTickAction();
			}

			if (tickDiff == 20)
			{
				if (config.getOnDeath() == MocapOnDeath.END_RECORDING) { selfStop(true); }
				else if (config.getOnDeath() != MocapOnDeath.SPLIT_RECORDING) { positionTracker.teleportFarAway(data.actions); }
			}
			return;
		}

		RecordedEntityState newEntityState = new RecordedEntityState(recordedPlayer);
		newEntityState.saveDifference(data.actions, entityState);
		entityState = newEntityState;

		positionTracker.onTick(data.actions, null);
		entityTracker.onTick();

		if (recordedPlayer.isDeadOrDying())
		{
			addAction(Die.INSTANCE);
			died = true;
			diedOnTick = tick;

			if (config.getOnDeath() != MocapOnDeath.END_RECORDING) { RecordingManager.waitingForRespawn.put(recordedPlayer, this); }
		}
		else if (recordedPlayer.level().dimension() != lastDimension)
		{
			onDimensionChange();
			lastDimension = recordedPlayer.level().dimension();
		}
		else if (recordedPlayer.isRemoved())
		{
			selfStop(false);
		}

		addTickAction();
	}

	private void onDimensionChange()
	{
		switch (config.getOnChangeDimension())
		{
			case NOTHING:
				break;

			case END_RECORDING:
				selfStop(true);
				break;

			case SPLIT_RECORDING:
				splitRecording(recordedPlayer);
				break;

			default:
				throw new RuntimeException("Unknown state: " + config.getOnChangeDimension());
		}
	}

	public void onRespawn(ServerPlayer newPlayer)
	{
		if (config.getOnDeath() == MocapOnDeath.SPLIT_RECORDING)
		{
			splitRecording(newPlayer);
			return;
		}

		died = false;
		recordedPlayer = newPlayer;
		positionTracker.setEntity(newPlayer);
		addAction(Respawn.INSTANCE);
	}

	private void selfStop(boolean requiresSafeDiscard)
	{
		this.requiresSafeDiscard = requiresSafeDiscard;
		this.state = State.WAITING_FOR_DECISION;

		RecordingManager.sendStopMessage((msg) -> Utils.sendMessage(source.player, msg), this, source.player);
	}

	public void splitRecording(ServerPlayer newPlayer)
	{
		state = State.WAITING_FOR_DECISION;
		Utils.sendMessage(source.player, "recording.stop.split");

		boolean success = (RecordingManager.start(newPlayer, source, config, null, true, false) != null);
		if (!success) { Utils.sendMessage(source.player, "recording.stop.split.error"); }
	}

	@Override public void addAction(MocapAction action)
	{
		if (state != State.RECORDING)
		{
			if (state == State.WAITING_FOR_ACTION) { start(true); }
			else { return; }
		}

		data.actions.add(action);
		if (action instanceof MocapBlockAction) { data.blockActions.add((MocapBlockAction)action); }
	}

	@Override public void addEntityAction(MocapAction action, int entityId)
	{
		addAction(new EntityAction(entityId, action));
	}

	@Override public Collection<? extends TrackedEntity> getTrackedEntities()
	{
		return entityTracker.getAll();
	}

	public void addTickAction()
	{
		int lastElementPos = data.actions.size() - 1;
		if (lastElementPos < 0)
		{
			addAction(NextTick.INSTANCE);
			return;
		}

		MocapAction lastElement = data.actions.get(lastElementPos);

		if (lastElement instanceof NextTick)
		{
			data.actions.set(lastElementPos, new SkipTicks(2));
		}
		else if (lastElement instanceof SkipTicks skipTicks && skipTicks.canBeModified())
		{
			data.actions.set(lastElementPos, skipTicks.increment());
		}
		else
		{
			addAction(NextTick.INSTANCE);
		}
	}

	public @Nullable EntityTracker.TrackedEntity getTrackedEntity(Entity entity)
	{
		return entityTracker.get(entity);
	}

	public int getTick()
	{
		return tick;
	}

	public boolean isRemoved()
	{
		return state.removed;
	}

	public enum State
	{
		WAITING_FOR_ACTION(false),
		RECORDING(false),
		WAITING_FOR_DECISION(false),
		CANCELED(true),
		DISCARDED(true),
		SAVED(true),
		UNDEFINED(true);

		public final boolean removed;

		State(boolean removed)
		{
			this.removed = removed;
		}
	}
}
