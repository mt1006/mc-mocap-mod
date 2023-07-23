package com.mt1006.mocap.mocap.recording;

import com.mt1006.mocap.command.CommandInfo;
import com.mt1006.mocap.mixin.fields.LevelMixin;
import com.mt1006.mocap.mocap.actions.EntityUpdate;
import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.Playing;
import com.mt1006.mocap.mocap.settings.Settings;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Recording
{
	public enum State
	{
		NOT_RECORDING,
		WAITING_FOR_ACTION,
		RECORDING,
		WAITING_FOR_DECISION
	}

	public static RecordingFiles.Writer writer = new RecordingFiles.Writer();
	public static State state = State.NOT_RECORDING;
	public static ServerPlayer player = null;
	private static @Nullable ServerPlayer commandSourcePlayer = null;
	private static @Nullable EntityState previousPlayerState = null;
	private static @Nullable EntityState previousActions = null;
	private static final Map<Entity, TrackedEntity> trackedEntityMap = new HashMap<>();
	private static int trackedEntityCounter = 0;
	private static int trackingTick = 0;
	private static Entity playerVehicle;
	public static int endsWithDeathPos = -1;

	public static boolean start(CommandInfo commandInfo, ServerPlayer serverPlayer)
	{
		switch (state)
		{
			case NOT_RECORDING:
				commandSourcePlayer = commandInfo.source != null ? commandInfo.source.getPlayer() : null;
				player = serverPlayer;
				commandInfo.sendSuccess("mocap.recording.start.waiting_for_action");
				state = State.WAITING_FOR_ACTION;
				break;

			case WAITING_FOR_ACTION:
				if (serverPlayer != player)
				{
					commandInfo.sendFailure("mocap.recording.start.different_player");
					commandInfo.sendFailure("mocap.recording.start.different_player.tip");
					return false;
				}

				previousPlayerState = null;
				state = State.RECORDING;
				commandInfo.sendSuccess("mocap.recording.start.recording_started");
				break;

			case RECORDING:
				commandInfo.sendFailure("mocap.recording.start.already_recording");
				commandInfo.sendFailure("mocap.recording.start.already_recording.tip");
				return false;

			case WAITING_FOR_DECISION:
				commandInfo.sendFailure("mocap.recording.stop.waiting_for_decision");
				return false;
		}
		return true;
	}

	public static boolean stop(CommandInfo commandInfo)
	{
		if (Settings.RECORDING_SYNC.val) { Playing.stopAll(commandInfo); }

		switch (state)
		{
			case NOT_RECORDING:
				commandInfo.sendFailure("mocap.recording.stop.server_not_recording");
				return false;

			case WAITING_FOR_ACTION:
				commandInfo.sendSuccess("mocap.recording.stop.stop_waiting_for_action");
				state = State.NOT_RECORDING;
				break;

			case RECORDING:
				commandInfo.sendSuccess("mocap.recording.stop.waiting_for_decision");
				state = State.WAITING_FOR_DECISION;
				break;

			case WAITING_FOR_DECISION:
				writer.clear();
				commandInfo.sendSuccess("mocap.recording.stop.recording_discarded");
				state = State.NOT_RECORDING;
				break;
		}
		return true;
	}

	public static boolean save(CommandInfo commandInfo, String name)
	{
		switch (state)
		{
			case NOT_RECORDING:
				commandInfo.sendFailure("mocap.recording.save.nothing_to_save");
				commandInfo.sendFailure("mocap.recording.save.nothing_to_save.tip");
				return false;

			case WAITING_FOR_ACTION:
				commandInfo.sendFailure("mocap.recording.save.waiting_for_action");
				commandInfo.sendFailure("mocap.recording.save.waiting_for_action.tip");
				return false;

			case RECORDING:
				commandInfo.sendFailure("mocap.recording.save.recording_not_stopped");
				commandInfo.sendFailure("mocap.recording.save.recording_not_stopped.tip");
				return false;

			case WAITING_FOR_DECISION:
				if (RecordingFiles.save(commandInfo, name, writer)) { state = State.NOT_RECORDING; }
				else { return false; }
				break;
		}
		return true;
	}

	public static boolean state(CommandInfo commandInfo)
	{
		switch (state)
		{
			case NOT_RECORDING:
				commandInfo.sendSuccess("mocap.recording.state.not_recording");
				break;

			case WAITING_FOR_ACTION:
				commandInfo.sendSuccess("mocap.recording.state.waiting_for_action");
				commandInfo.sendSuccess("mocap.recording.state.player_name");
				break;

			case RECORDING:
				commandInfo.sendSuccess("mocap.recording.state.recording");
				commandInfo.sendSuccess("mocap.recording.state.player_name", player.getName());
				break;

			case WAITING_FOR_DECISION:
				commandInfo.sendSuccess("mocap.recording.state.waiting_for_decision");
				break;
		}
		return true;
	}

	public static void onTick()
	{
		if (state == State.WAITING_FOR_ACTION)
		{
			EntityState entityState = new EntityState(player);
			if (entityState.differs(previousActions))
			{
				previousPlayerState = null;
				state = State.RECORDING;
				Utils.sendSystemMessage(commandSourcePlayer, "mocap.recording.start.recording_started");

				previousActions = null;
				trackedEntityMap.clear();
				trackedEntityCounter = 0;
				trackingTick = 0;
				playerVehicle = null;
			}
			else
			{
				previousActions = entityState;
			}
		}

		if (state == State.RECORDING)
		{
			EntityState playerState = new EntityState(player);
			playerState.saveDifference(writer, previousPlayerState);
			previousPlayerState = playerState;

			if (player.isDeadOrDying() && Settings.RECORD_PLAYER_DEATH.val)
			{
				writer.modifyBoolean(endsWithDeathPos, true);
				Utils.sendSystemMessage(commandSourcePlayer, "mocap.recording.stop.waiting_for_decision");
				state = State.WAITING_FOR_DECISION;
				return;
			}

			if (Settings.ENTITY_TRACKING_DISTANCE.val == 0.0)
			{
				if (trackedEntityMap.size() > 0)
				{
					trackedEntityMap.forEach((uuid, trackedEntity) -> new EntityUpdate(EntityUpdate.REMOVE, trackedEntity.id).write(writer));
					trackedEntityMap.clear();
				}
				trackingTick++;
				return;
			}

			boolean limitDistance = Settings.ENTITY_TRACKING_DISTANCE.val > 0.0;
			double maxDistanceSqr = Settings.ENTITY_TRACKING_DISTANCE.val * Settings.ENTITY_TRACKING_DISTANCE.val;

			for (Entity entity : ((LevelMixin)player.level).callGetEntities().getAll())
			{
				if (limitDistance && player.distanceToSqr(entity) > maxDistanceSqr || entity instanceof Player
						|| (!Settings.TRACK_PLAYED_ENTITIES.val && entity.getTags().contains(Playing.MOCAP_ENTITY_TAG)))
				{
					continue;
				}

				if (entity instanceof Saddleable || entity instanceof Minecart || entity instanceof Boat)
				{
					if (!Settings.TRACK_VEHICLE_ENTITIES.val) { continue; }
				}
				else if (entity instanceof ItemEntity)
				{
					if (!Settings.TRACK_ITEM_ENTITIES.val) { continue; }
				}
				else if (!Settings.TRACK_OTHER_ENTITIES.val)
				{
					continue;
				}

				TrackedEntity trackedEntity = trackedEntityMap.get(entity);
				if (trackedEntity == null)
				{
					trackedEntity = new TrackedEntity(trackedEntityCounter++);
					trackedEntityMap.put(entity, trackedEntity);
					new EntityUpdate(EntityUpdate.ADD, trackedEntity.id, entity).write(writer);
				}
				trackedEntity.onTick(writer, entity, trackingTick);
			}

			Entity newPlayerVehicle = player.getVehicle();
			if (newPlayerVehicle != null)
			{
				if (!newPlayerVehicle.equals(playerVehicle))
				{
					if (playerVehicle != null)
					{
						new EntityUpdate(EntityUpdate.PLAYER_DISMOUNT).write(writer);
						playerVehicle = null;
					}

					TrackedEntity trackedEntity = trackedEntityMap.get(newPlayerVehicle);
					if (trackedEntity != null)
					{
						new EntityUpdate(EntityUpdate.PLAYER_MOUNT, trackedEntity.id).write(writer);
						playerVehicle = newPlayerVehicle;
					}
				}
			}
			else if (playerVehicle != null)
			{
				new EntityUpdate(EntityUpdate.PLAYER_DISMOUNT).write(writer);
				playerVehicle = null;
			}

			List<Entity> toRemove = new ArrayList<>();
			for (Map.Entry<Entity, TrackedEntity> entry : trackedEntityMap.entrySet())
			{
				if (entry.getValue().lastTick == trackingTick) { continue; }

				if (!entry.getValue().dying)
				{
					byte updateType = entry.getKey().getRemovalReason() == Entity.RemovalReason.KILLED ? EntityUpdate.KILL : EntityUpdate.REMOVE;
					new EntityUpdate(updateType, entry.getValue().id).write(writer);
				}
				toRemove.add(entry.getKey());
			}
			toRemove.forEach(trackedEntityMap::remove);
			trackingTick++;
		}
	}

	public static boolean isRecordedPlayer(@Nullable Entity entity)
	{
		return (entity instanceof ServerPlayer) && entity.equals(player);
	}

	public static @Nullable TrackedEntity getTrackedEntity(Entity entity)
	{
		return trackedEntityMap.get(entity);
	}
}
