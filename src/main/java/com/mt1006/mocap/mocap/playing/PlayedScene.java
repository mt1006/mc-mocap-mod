package com.mt1006.mocap.mocap.playing;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mt1006.mocap.command.CommandInfo;
import com.mt1006.mocap.events.PlayerConnectionEvent;
import com.mt1006.mocap.mocap.actions.Action;
import com.mt1006.mocap.mocap.recording.EntityState;
import com.mt1006.mocap.mocap.recording.Recording;
import com.mt1006.mocap.mocap.settings.Settings;
import com.mt1006.mocap.network.MocapPacketS2C;
import com.mt1006.mocap.utils.EntityData;
import com.mt1006.mocap.utils.FakePlayer;
import com.mt1006.mocap.utils.Fields;
import com.mt1006.mocap.utils.ProfileUtils;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
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

	public boolean start(CommandInfo commandInfo, String name, PlayerData playerData, int id)
	{
		this.root = true;
		this.id = id;
		this.name = name;
		this.playerData = playerData;
		this.type = name.charAt(0) == '.' ? SceneType.SCENE : SceneType.RECORDING;
		this.level = commandInfo.source.getLevel();

		DataManager data = new DataManager();
		if (!data.load(commandInfo, name))
		{
			if (!data.knownError)
			{
				commandInfo.sendFailure("mocap.playing.start.error");
				commandInfo.sendFailure("mocap.playing.start.error.load");
			}
			commandInfo.sendFailure("mocap.playing.start.error.load.path", data.getResourcePath());
			return false;
		}

		switch (type)
		{
			case RECORDING: return startPlayingRecording(commandInfo, data);
			case SCENE: return startPlayingScene(commandInfo, data);
			default: return false;
		}
	}

	private boolean startSubscene(CommandInfo commandInfo, PlayedScene parent, SceneData.Subscene info, DataManager data)
	{
		this.root = false;
		this.id = 0;
		this.name = info.name;
		this.level = commandInfo.source.getLevel();
		this.type = name.charAt(0) == '.' ? SceneType.SCENE : SceneType.RECORDING;

		playerData = info.playerData.mergeWithParent(parent.playerData);
		playerAsEntityID = info.playerAsEntityID != null ? info.playerAsEntityID : parent.playerAsEntityID;
		offset = parent.offset.add(info.posOffset[0], info.posOffset[1], info.posOffset[2]);
		startDelay = (int)Math.round(info.startDelay * 20.0);

		switch (type)
		{
			case RECORDING: return startPlayingRecording(commandInfo, data);
			case SCENE: return startPlayingScene(commandInfo, data);
			default: return false;
		}
	}

	private boolean startPlayingScene(CommandInfo commandInfo, DataManager data)
	{
		SceneData sceneData = data.getScene(name);
		if (sceneData == null) { return false; }

		for (SceneData.Subscene subscene : sceneData.subscenes)
		{
			PlayedScene newScene = new PlayedScene();
			if (!newScene.startSubscene(commandInfo, this, subscene, data)) { return false; }
			subscenes.add(newScene);
		}
		return true;
	}

	private boolean startPlayingRecording(CommandInfo commandInfo, DataManager data)
	{
		GameProfile profile = getGameProfile(commandInfo);
		if (profile == null)
		{
			commandInfo.sendFailure("mocap.playing.start.error");
			commandInfo.sendFailure("mocap.playing.start.error.profile");
			return false;
		}

		GameProfile newProfile = new GameProfile(UUID.randomUUID(), profile.getName());
		try
		{
			PropertyMap oldPropertyMap = (PropertyMap)Fields.gameProfileProperties.get(profile);
			PropertyMap newPropertyMap = (PropertyMap)Fields.gameProfileProperties.get(newProfile);

			newPropertyMap.putAll(oldPropertyMap);
			playerData.addSkinToPropertyMap(commandInfo, newPropertyMap);
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

			packetTargets.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, fakePlayer));
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

			EntityData.PLAYER_SKIN_PARTS.set(fakePlayer,(byte)0b01111111);
		}
		else
		{
			ResourceLocation entityRes = new ResourceLocation(playerAsEntityID);
			EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(entityRes);
			entity = (ForgeRegistries.ENTITIES.containsKey(entityRes) && entityType != null) ? entityType.create(level) : null;

			if (entity == null)
			{
				//TODO: better message (and fall back to FakePlayer)
				commandInfo.sendFailure("mocap.playing.start.warning.unknown_entity");
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

	private @Nullable GameProfile getGameProfile(CommandInfo commandInfo)
	{
		String profileName = playerData.name;
		Entity entity = commandInfo.source.getEntity();
		Level level = commandInfo.source.getLevel();

		if (profileName == null)
		{
			if (entity instanceof ServerPlayer) { profileName = ((ServerPlayer)entity).getGameProfile().getName(); }
			else if (!level.players().isEmpty()) { profileName = level.players().get(0).getGameProfile().getName(); }
			else { profileName = "Player"; }
		}

		return ProfileUtils.getGameProfile(commandInfo.source.getServer(), profileName);
	}

	private enum SceneType
	{
		SCENE,
		RECORDING
	}
}
