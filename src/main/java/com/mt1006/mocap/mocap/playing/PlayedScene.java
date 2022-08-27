package com.mt1006.mocap.mocap.playing;

import com.mojang.authlib.GameProfile;
import com.mt1006.mocap.mocap.commands.Recording;
import com.mt1006.mocap.mocap.commands.Settings;
import com.mt1006.mocap.utils.FakePlayer;
import com.mt1006.mocap.utils.ProfileUtils;
import com.mt1006.mocap.utils.RecordingUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class PlayedScene
{
	private int id;
	private SceneType type;
	private String name;
	private boolean root;
	private PlayerList packetTargets;
	private ServerLevel world;

	private String profileName = null;
	private double offsetX = 0.0;
	private double offsetY = 0.0;
	private double offsetZ = 0.0;
	private int startDelay = 0;
	private int tickCounter = 0;
	public boolean finished = false;

	// SCENE
	private ArrayList<PlayedScene> subscenes;

	// RECORDING
	private FakePlayer entity = null;
	private RecordingUtils.RecordingReader reader = new RecordingUtils.RecordingReader();

	public boolean start(CommandSourceStack commandSource, String name, int id)
	{
		root = true;
		this.id = id;
		this.name = name;
		world = commandSource.getLevel();
		packetTargets = commandSource.getLevel().getServer().getPlayerList();

		if (name.charAt(0) == '.') { type = SceneType.SCENE; }
		else { type = SceneType.RECORDING; }

		SceneData data = new SceneData();
		if (!data.load(commandSource, name))
		{
			if (!data.knownError)
			{
				commandSource.sendFailure(new TranslatableComponent("mocap.commands.playing.start.error"));
				commandSource.sendFailure(new TranslatableComponent("mocap.commands.playing.start.error.load"));
			}
			commandSource.sendFailure(new TranslatableComponent("mocap.commands.playing.start.error.load.path", data.getResourcePath()));
			return false;
		}

		switch (type)
		{
			case RECORDING:
				if (!startPlayingRecording(commandSource, data)) { return false; }
				break;

			case SCENE:
				if (!startPlayingScene(commandSource, data)) { return false; }
				break;
		}
		return true;
	}

	private boolean startSubscene(CommandSourceStack commandSource, PlayedScene parent, SceneInfo.Subscene info, SceneData data)
	{
		root = false;
		id = 0;
		name = info.getName();
		world = commandSource.getLevel();
		packetTargets = commandSource.getLevel().getServer().getPlayerList();

		if (name.charAt(0) == '.') { type = SceneType.SCENE; }
		else { type = SceneType.RECORDING; }

		offsetX = parent.offsetX + info.startPos[0];
		offsetY = parent.offsetY + info.startPos[1];
		offsetZ = parent.offsetZ + info.startPos[2];
		startDelay = (int)Math.round(info.startDelay * 20.0);

		profileName = info.playerName;
		if (profileName == null) { profileName = parent.profileName; }

		switch (type)
		{
			case RECORDING:
				if (!startPlayingRecording(commandSource, data)) { return false; }
				break;

			case SCENE:
				if (!startPlayingScene(commandSource, data)) { return false; }
				break;
		}
		return true;
	}

	private boolean startPlayingScene(CommandSourceStack commandSource, SceneData data)
	{
		subscenes = new ArrayList<>();

		SceneInfo sceneInfo = data.getScene(name);
		for (SceneInfo.Subscene subscene : sceneInfo.subscenes)
		{
			PlayedScene newScene = new PlayedScene();
			if (!newScene.startSubscene(commandSource, this, subscene, data)) { return false; }
			subscenes.add(newScene);
		}

		return true;
	}

	private boolean startPlayingRecording(CommandSourceStack commandSource, SceneData data)
	{
		GameProfile profile = getGameProfile(commandSource);
		if (profile == null)
		{
			commandSource.sendFailure(new TranslatableComponent("mocap.commands.playing.start.error"));
			commandSource.sendFailure(new TranslatableComponent("mocap.commands.playing.start.error.profile"));
			return false;
		}

		reader.recording = data.getRecording(name);

		FakePlayer fakePlayer = new FakePlayer(world, profile);
		if (!PlayerState.readHeader(fakePlayer, reader, new Vec3(offsetX, offsetY, offsetZ)))
		{
			commandSource.sendFailure(new TranslatableComponent("mocap.commands.playing.start.error"));
			commandSource.sendFailure(new TranslatableComponent("mocap.commands.playing.start.error.load_header"));
			return false;
		}

		entity = fakePlayer;
		packetTargets.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, entity));
		packetTargets.broadcastAll(new ClientboundAddPlayerPacket(entity));

		//https://wiki.vg/index.php?title=Entity_metadata&oldid=17519#Player
		SynchedEntityData dataManager = new SynchedEntityData(entity);
		EntityDataAccessor<Byte> skinPartsParameter = new EntityDataAccessor<>(17, EntityDataSerializers.BYTE);
		dataManager.define(skinPartsParameter, (byte)0b01111111);

		packetTargets.broadcastAll(new ClientboundSetEntityDataPacket(entity.getId(), dataManager, true));
		return true;
	}

	public void stop()
	{
		if (root)
		{
			ArrayList<Integer> IDs = new ArrayList<>();
			removeEntities(IDs);

			int[] arrayOfIDs = new int[IDs.size()];
			for (int i = 0; i < IDs.size(); i++)
			{
				arrayOfIDs[i] = IDs.get(i);
			}

			packetTargets.broadcastAll(new ClientboundRemoveEntitiesPacket(arrayOfIDs));
			finished = true;
		}
	}

	public boolean onTick()
	{
		if (finished) { return true; }

		if ((startDelay <= tickCounter && (!Settings.RECORDING_SYNC.val ||
				Recording.state == Recording.State.RECORDING)) || tickCounter == 0)
		{
			switch (type)
			{
				case RECORDING:
					while (true)
					{
						int retVal = PlayerState.readState(packetTargets, entity, reader);

						if (retVal < 0) { finished = true; }
						if (retVal <= 0) { break; }
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

		if (root && finished) { stop(); }

		tickCounter++;
		return finished;
	}

	public int getID()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	private void removeEntities(ArrayList<Integer> list)
	{
		switch (type)
		{
			case RECORDING:
				packetTargets.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, entity));
				list.add(entity.getId());
				break;

			case SCENE:
				for (PlayedScene playedScene : subscenes)
				{
					playedScene.removeEntities(list);
				}
				break;
		}
	}

	@Nullable
	private GameProfile getGameProfile(CommandSourceStack commandSource)
	{
		if (profileName == null)
		{
			if (commandSource.getEntity() instanceof ServerPlayer)
			{
				return ((ServerPlayer)commandSource.getEntity()).getGameProfile();
			}
			else if (!commandSource.getLevel().players().isEmpty())
			{
				return commandSource.getLevel().players().get(0).getGameProfile();
			}
			else
			{
				return null;
			}
		}
		else
		{
			return ProfileUtils.getGameProfile(commandSource.getServer(), profileName);
		}
	}
}
