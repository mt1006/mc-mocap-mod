package net.mt1006.mocap.mocap.playing.playback;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.extension.MocapPositionTransformer;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.modifiers.MocapEntityFilter;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.events.PlayerConnectionEvent;
import net.mt1006.mocap.mocap.playing.PlaybackManager;
import net.mt1006.mocap.mocap.settings.Settings;
import net.mt1006.mocap.network.MocapPacketS2C;
import net.mt1006.mocap.utils.FakePlayer;
import net.mt1006.mocap.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class ActionContext implements MocapActionContext
{
	private final MocapRecordingData recordingData;
	private final ServerPlayer owner;
	private final PlayerList packetTargets;
	private final EntityData mainEntityData;
	private final Map<Integer, EntityData> entityDataMap = new HashMap<>();
	private final ServerLevel level;
	private final MocapPlaybackConfig config;
	private final MocapModifiers modifiers;
	private final @Nullable FakePlayer ghostPlayer;
	private final PositionTransformer transformer;
	private boolean mainEntityRemoved = false;
	private @Nullable EntityData currentEntityData = null;
	private Entity entity;
	private Vec3 position;
	private int repeatCounter = 0;

	public ActionContext(MocapRecordingData recordingData, ServerPlayer owner, PlayerList packetTargets, Entity entity,
						 MocapPlaybackConfig config, MocapModifiers modifiers, @Nullable FakePlayer ghostPlayer, PositionTransformer transformer)
	{
		if (!(entity.level() instanceof ServerLevel))
		{
			throw new RuntimeException("Failed to get ServerLevel for ActionContext!");
		}

		this.recordingData = recordingData;
		this.owner = owner;
		this.packetTargets = packetTargets;
		this.mainEntityData = new EntityData(entity, recordingData.getStartPos());
		this.level = (ServerLevel) entity.level();
		this.config = config;
		this.modifiers = modifiers;
		this.ghostPlayer = ghostPlayer;
		this.transformer = transformer;

		setMainContextEntity();
	}

	@Override public MocapRecordingData getRecordingData()
	{
		return recordingData;
	}

	@Override public Entity getEntity()
	{
		return entity;
	}

	@Override public ServerLevel getLevel()
	{
		return level;
	}

	@Override public MocapPlaybackConfig getConfig()
	{
		return config;
	}

	@Override public MocapModifiers getModifiers()
	{
		return modifiers;
	}

	@Override public MocapPositionTransformer getTransformer()
	{
		return transformer;
	}

	@Override public Entity getMainEntity()
	{
		return mainEntityData.entity;
	}

	@Override public @Nullable ServerPlayer getDummyPlayer()
	{
		return ghostPlayer;
	}

	@Override public @Nullable ServerPlayer getRealOrDummyPlayer()
	{
		return (entity instanceof ServerPlayer) ? (ServerPlayer)entity : ghostPlayer;
	}

	@Override public @Nullable ServerPlayer getLivingEntityOrDummyPlayer()
	{
		return (entity instanceof ServerPlayer) ? (ServerPlayer)entity : ghostPlayer;
	}

	@Override public void setMainContextEntity()
	{
		setContextEntity(mainEntityData);
	}

	@Override public boolean setContextEntity(int id)
	{
		EntityData data = entityDataMap.get(id);
		if (data == null) { return false; }

		setContextEntity(data);
		return true;
	}

	private void setContextEntity(EntityData data)
	{
		if (currentEntityData != null) { currentEntityData.lastPosition = position; }
		currentEntityData = data;

		entity = data.entity;
		position = data.lastPosition;
	}

	@Override public void broadcast(Packet<?> packet)
	{
		packetTargets.broadcastAll(packet);
	}

	@Override public void fluentMovement(Supplier<Packet<?>> packetSupplier)
	{
		double fluentMovements = Settings.FLUENT_MOVEMENTS.val;
		if (fluentMovements == 0.0) { return; }
		Packet<?> packet = packetSupplier.get();

		if (fluentMovements > 0.0)
		{
			Vec3 pos = entity.position();
			double maxDistSqr = fluentMovements * fluentMovements;

			for (ServerPlayer player : packetTargets.getPlayers())
			{
				if (player.distanceToSqr(pos) > maxDistSqr) { continue; }
				player.connection.send(packet);
			}
		}
		else
		{
			packetTargets.broadcastAll(packet);
		}
	}

	public void removeAdditionalEntities()
	{
		entityDataMap.values().forEach((data) -> removeEntity(data.entity, level));
		entityDataMap.clear();
	}

	public void removeMainEntity()
	{
		if (mainEntityRemoved) { return; }
		mainEntityRemoved = true;

		FakePlayer playerToRemove;
		if (entity instanceof Player)
		{
			if (!(entity instanceof FakePlayer))
			{
				Utils.sendMessage(owner, "error.failed_to_remove_fake_player");
				MocapMod.LOGGER.error("Failed to remove fake player!");
				return;
			}
			playerToRemove = (FakePlayer)entity;
		}
		else
		{
			removeEntity(entity, level);
			if (ghostPlayer == null) { return; }
			playerToRemove = ghostPlayer;
		}

		UUID uuid = playerToRemove.getUUID();
		if (PlayerConnectionEvent.nocolPlayers.contains(uuid))
		{
			for (ServerPlayer player : PlayerConnectionEvent.players)
			{
				MocapPacketS2C.sendNocolPlayerRemove(player, uuid);
				PlayerConnectionEvent.removeNocolPlayer(uuid);
			}
		}
		if (playerToRemove != ghostPlayer) { broadcast(new ClientboundPlayerInfoRemovePacket(List.of(uuid))); }
		playerToRemove.remove(Entity.RemovalReason.KILLED);
		playerToRemove.getAdvancements().stopListening();
	}

	@Override public Vec3 getPosition()
	{
		return position;
	}

	@Override public void changePosition(Vec3 newPos, float rotY, float rotX, boolean transformRot)
	{
		position = newPos;
		Vec3 finPos = transformer.transformPos(position);
		float finRotY = transformRot ? transformer.transformRotation(rotY) : rotY;

		entity.snapTo(finPos, finRotY, rotX);
		if (ghostPlayer != null && entity == mainEntityData.entity) { ghostPlayer.snapTo(finPos, finRotY, rotX); }
	}

	@Override public MocapEntityFilter getEntityFilter()
	{
		MocapEntityFilter filter = modifiers.getEntityFilter();
		return filter != null ? filter : config.getPlayEntities();
	}

	@Override public void addEntity(int id, Entity entity, Vec3 position)
	{
		entityDataMap.put(id, new EntityData(entity, position));
	}

	@Override public @Nullable EntityData getEntityData(int id)
	{
		return entityDataMap.get(id);
	}

	@Override public boolean hasEntity(int id)
	{
		return entityDataMap.containsKey(id);
	}

	@Override public void incrementRepeatCounter()
	{
		repeatCounter++;
	}

	@Override public boolean shouldStopRepeat(int iter)
	{
		if (repeatCounter == iter)
		{
			repeatCounter = 0;
			return true;
		}
		return false;
	}

	private void removeEntity(Entity entity, ServerLevel level)
	{
		switch (config.getEntitiesAfterPlayback())
		{
			case REMOVE:
				entity.remove(Entity.RemovalReason.KILLED);

			case KILL:
				entity.invulnerableTime = 0; // for sound effect
				if (entity instanceof FakePlayer) { ((FakePlayer)entity).fakeKill(); }
				else { entity.kill(level); }
				break;

			case LEFT_UNTOUCHED:
				break;

			case RELEASE_AS_NORMAL:
				entity.setNoGravity(false);
				entity.setInvulnerable(false);
				entity.removeTag(PlaybackManager.MOCAP_ENTITY_TAG);
				if (entity instanceof Mob) { ((Mob)entity).setNoAi(false); }
				break;

			default:
				throw new IllegalStateException("Unexpected value: " + config.getEntitiesAfterPlayback());
		}
	}
}
