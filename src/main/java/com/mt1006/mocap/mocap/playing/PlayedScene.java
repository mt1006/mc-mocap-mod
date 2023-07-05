package com.mt1006.mocap.mocap.playing;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mt1006.mocap.events.PlayerConnectionEvent;
import com.mt1006.mocap.mocap.actions.Action;
import com.mt1006.mocap.mocap.recording.Recording;
import com.mt1006.mocap.mocap.settings.Settings;
import com.mt1006.mocap.network.MocapPacketS2C;
import com.mt1006.mocap.utils.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayedScene
{
	private int id;
	private SceneType type;
	private String name;
	private boolean root;
	private ServerLevel level;

	private PlayerData playerData = null;
	private @Nullable String playerAsEntityID = null;
	private Vec3 offset = Vec3.ZERO;
	private int startDelay = 0;
	private int tickCounter = 0;
	private int dyingTicks = 0;
	private boolean finished = false;

	// SCENE
	private final List<PlayedScene> subscenes = new ArrayList<>();

	// RECORDING
	private RecordingData recording;
	private PlayingContext ctx;
	private int pos = 0;

	public boolean start(CommandSourceStack commandSource, String name, PlayerData playerData, int id)
	{
		this.root = true;
		this.id = id;
		this.name = name;
		this.level = commandSource.getLevel();

		this.playerData = playerData;

		if (name.charAt(0) == '.') { type = SceneType.SCENE; }
		else { type = SceneType.RECORDING; }

		DataManager data = new DataManager();
		if (!data.load(commandSource, name))
		{
			if (!data.knownError)
			{
				Utils.sendFailure(commandSource, "mocap.playing.start.error");
				Utils.sendFailure(commandSource, "mocap.playing.start.error.load");
			}
			Utils.sendFailure(commandSource, "mocap.playing.start.error.load.path", data.getResourcePath());
			return false;
		}

		switch (type)
		{
			case RECORDING: return startPlayingRecording(commandSource, data);
			case SCENE: return startPlayingScene(commandSource, data);
			default: return false;
		}
	}

	private boolean startSubscene(CommandSourceStack commandSource, PlayedScene parent, SceneData.Subscene info, DataManager data)
	{
		root = false;
		id = 0;
		name = info.name;
		level = commandSource.getLevel();

		if (name.charAt(0) == '.') { type = SceneType.SCENE; }
		else { type = SceneType.RECORDING; }

		playerData = info.playerData.mergeWithParent(parent.playerData);
		playerAsEntityID = info.playerAsEntityID != null ? info.playerAsEntityID : parent.playerAsEntityID;
		offset = parent.offset.add(info.posOffset[0], info.posOffset[1], info.posOffset[2]);
		startDelay = (int)Math.round(info.startDelay * 20.0);

		switch (type)
		{
			case RECORDING: return startPlayingRecording(commandSource, data);
			case SCENE: return startPlayingScene(commandSource, data);
			default: return false;
		}
	}

	private boolean startPlayingScene(CommandSourceStack commandSource, DataManager data)
	{
		SceneData sceneData = data.getScene(name);
		if (sceneData == null) { return false; }

		for (SceneData.Subscene subscene : sceneData.subscenes)
		{
			PlayedScene newScene = new PlayedScene();
			if (!newScene.startSubscene(commandSource, this, subscene, data)) { return false; }
			subscenes.add(newScene);
		}
		return true;
	}

	private boolean startPlayingRecording(CommandSourceStack commandSource, DataManager data)
	{
		GameProfile profile = getGameProfile(commandSource);
		if (profile == null)
		{
			Utils.sendFailure(commandSource, "mocap.playing.start.error");
			Utils.sendFailure(commandSource, "mocap.playing.start.error.profile");
			return false;
		}

		GameProfile newProfile = new GameProfile(UUID.randomUUID(), profile.getName());
		try
		{
			PropertyMap oldPropertyMap = (PropertyMap)Fields.gameProfileProperties.get(profile);
			PropertyMap newPropertyMap = (PropertyMap)Fields.gameProfileProperties.get(newProfile);

			newPropertyMap.putAll(oldPropertyMap);
			playerData.addSkinToPropertyMap(commandSource, newPropertyMap);
		}
		catch (Exception ignore) {}

		recording = data.getRecording(name);
		if (recording == null) { return false; }

		PlayerList packetTargets = level.getServer().getPlayerList();
		Vec3i blockOffset = new Vec3i((int)Math.round(offset.x), (int)Math.round(offset.y), (int)Math.round(offset.z));
		Entity entity;

		if (playerAsEntityID == null)
		{
			FakePlayer fakePlayer = new FakePlayer(level, newProfile);
			entity = fakePlayer;

			fakePlayer.gameMode.changeGameModeForPlayer(Settings.USE_CREATIVE_GAME_MODE.val ? GameType.CREATIVE : GameType.SURVIVAL);
			EntityState.initEntity(fakePlayer, recording, offset);

			recording.preExecute(fakePlayer, blockOffset);

			packetTargets.broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
			packetTargets.broadcastAll(new ClientboundAddPlayerPacket(fakePlayer));

			level.addNewPlayer(fakePlayer);

			if (!Settings.CAN_PUSH_ENTITIES.val)
			{
				for (ServerPlayer player : PlayerConnectionEvent.players)
				{
					MocapPacketS2C.sendNocolPlayerAdd(player, fakePlayer.getUUID());
					PlayerConnectionEvent.addNocolPlayer(fakePlayer.getUUID());
				}
			}

			new EntityData(fakePlayer, EntityData.PLAYER_SKIN_PARTS, (byte)0b01111111).broadcast(packetTargets);
		}
		else
		{
			ResourceLocation entityRes = new ResourceLocation(playerAsEntityID);
			EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityRes);
			entity = (ForgeRegistries.ENTITY_TYPES.containsKey(entityRes) && entityType != null) ? entityType.create(level) : null;

			if (entity == null)
			{
				//TODO: better message (and fall back to FakePlayer)
				Utils.sendFailure(commandSource, "mocap.playing.start.warning.unknown_entity");
				return false;
			}

			EntityState.initEntity(entity, recording, offset);
			entity.setDeltaMovement(0.0, 0.0, 0.0);
			entity.setInvulnerable(true);
			entity.setNoGravity(true);
			if (entity instanceof Mob) { ((Mob)entity).setNoAi(true); }

			level.addFreshEntity(entity);
			recording.preExecute(entity, blockOffset);
		}

		ctx = new PlayingContext(packetTargets, entity, offset, blockOffset);
		return true;
	}

	public void stop()
	{
		switch (type)
		{
			case RECORDING:
				ctx.removeEntities();
				break;

			case SCENE:
				subscenes.forEach(PlayedScene::stop);
				break;
		}
		finished = true;
	}

	public boolean onTick()
	{
		if (dyingTicks > 0)
		{
			dyingTicks--;
			if (dyingTicks == 0)
			{
				ctx.entity.remove(Entity.RemovalReason.KILLED);
				ctx.entityRemoved = true;
				stop();
				return true;
			}
			return false;
		}

		if (finished) { return true; }

		if ((startDelay <= tickCounter && (!Settings.RECORDING_SYNC.val || Recording.state == Recording.State.RECORDING)) || tickCounter == 0)
		{
			switch (type)
			{
				case RECORDING:
					while (true)
					{
						Action.Result result = recording.executeNext(ctx, pos++);

						if (result.endsPlayback)
						{
							if (recording.endsWithDeath)
							{
								ctx.entity.kill();
								if (ctx.entity instanceof LivingEntity) { dyingTicks = 20; }
							}
							finished = true;
						}

						if (result.endsTick) { break; }
					}
					break;

				case SCENE:
					finished = true;
					for (PlayedScene scene : subscenes)
					{
						if (!scene.onTick()) { finished = false; }
					}
					break;
			}
		}

		if (root && finished && dyingTicks == 0) { stop(); }

		tickCounter++;
		return finished && dyingTicks == 0;
	}

	public int getID()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public boolean isFinished()
	{
		return finished && dyingTicks == 0;
	}

	private @Nullable GameProfile getGameProfile(CommandSourceStack commandSource)
	{
		String profileName = playerData.name;

		if (profileName == null)
		{
			if (commandSource.getEntity() instanceof ServerPlayer)
			{
				profileName = ((ServerPlayer)commandSource.getEntity()).getGameProfile().getName();
			}
			else if (!commandSource.getLevel().players().isEmpty())
			{
				profileName = commandSource.getLevel().players().get(0).getGameProfile().getName();
			}
			else
			{
				profileName = "Player";
			}
		}

		return ProfileUtils.getGameProfile(commandSource.getServer(), profileName);
	}

	private enum SceneType
	{
		SCENE,
		RECORDING
	}
}
