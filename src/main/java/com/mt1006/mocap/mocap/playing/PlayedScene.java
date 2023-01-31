package com.mt1006.mocap.mocap.playing;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mt1006.mocap.mocap.commands.Recording;
import com.mt1006.mocap.mocap.commands.Settings;
import com.mt1006.mocap.utils.*;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SDestroyEntitiesPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.network.play.server.SSpawnPlayerPacket;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.server.ServerWorld;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class PlayedScene
{
	private static final String MINESKIN_API_URL = "https://api.mineskin.org/get/uuid/";

	private int id;
	private SceneType type;
	private String name;
	private boolean root;
	private PlayerList packetTargets;
	private ServerWorld level;

	private String playerName = null;
	private String mineskinURL = null;
	private double offsetX = 0.0;
	private double offsetY = 0.0;
	private double offsetZ = 0.0;
	private int startDelay = 0;
	private int tickCounter = 0;
	public boolean finished = false;

	// SCENE
	private final List<PlayedScene> subscenes = new ArrayList<>();

	// RECORDING
	private RecordingData recording;
	private FakePlayer fakePlayer;
	private Vector3i blockOffset = new Vector3i(0, 0, 0);
	private int pos = 0;

	public boolean start(CommandSource commandSource, String name, String playerName, String mineskinURL, int id)
	{
		this.root = true;
		this.id = id;
		this.name = name;
		this.level = commandSource.getLevel();
		this.packetTargets = level.getServer().getPlayerList();

		this.playerName = playerName;
		this.mineskinURL = mineskinURL;

		if (name.charAt(0) == '.') { type = SceneType.SCENE; }
		else { type = SceneType.RECORDING; }

		SceneData data = new SceneData();
		if (!data.load(commandSource, name))
		{
			if (!data.knownError)
			{
				Utils.sendFailure(commandSource, "mocap.commands.playing.start.error");
				Utils.sendFailure(commandSource, "mocap.commands.playing.start.error.load");
			}
			Utils.sendFailure(commandSource, "mocap.commands.playing.start.error.load.path", data.getResourcePath());
			return false;
		}

		switch (type)
		{
			case RECORDING: return startPlayingRecording(commandSource, data);
			case SCENE: return startPlayingScene(commandSource, data);
			default: return false;
		}
	}

	private boolean startSubscene(CommandSource commandSource, PlayedScene parent, SceneInfo.Subscene info, SceneData data)
	{
		root = false;
		id = 0;
		name = info.name;
		level = commandSource.getLevel();
		packetTargets = commandSource.getLevel().getServer().getPlayerList();

		if (name.charAt(0) == '.') { type = SceneType.SCENE; }
		else { type = SceneType.RECORDING; }

		offsetX = parent.offsetX + info.startPos[0];
		offsetY = parent.offsetY + info.startPos[1];
		offsetZ = parent.offsetZ + info.startPos[2];
		blockOffset = new Vector3i(Math.round(offsetX), Math.round(offsetY), Math.round(offsetZ));
		startDelay = (int)Math.round(info.startDelay * 20.0);
		playerName = info.playerName != null ? info.playerName : parent.playerName;
		mineskinURL = info.mineskinURL != null ? info.mineskinURL : parent.mineskinURL;

		switch (type)
		{
			case RECORDING: return startPlayingRecording(commandSource, data);
			case SCENE: return startPlayingScene(commandSource, data);
			default: return false;
		}
	}

	private boolean startPlayingScene(CommandSource commandSource, SceneData data)
	{
		SceneInfo sceneInfo = data.getScene(name);
		if (sceneInfo == null) { return false; }

		for (SceneInfo.Subscene subscene : sceneInfo.subscenes)
		{
			PlayedScene newScene = new PlayedScene();
			if (!newScene.startSubscene(commandSource, this, subscene, data)) { return false; }
			subscenes.add(newScene);
		}
		return true;
	}

	private boolean startPlayingRecording(CommandSource commandSource, SceneData data)
	{
		GameProfile profile = getGameProfile(commandSource);
		if (profile == null)
		{
			Utils.sendFailure(commandSource, "mocap.commands.playing.start.error");
			Utils.sendFailure(commandSource, "mocap.commands.playing.start.error.profile");
			return false;
		}

		GameProfile newProfile = new GameProfile(UUID.randomUUID(), profile.getName());
		try
		{
			PropertyMap oldPropertyMap = (PropertyMap)Fields.gameProfileProperties.get(profile);
			PropertyMap newPropertyMap = (PropertyMap)Fields.gameProfileProperties.get(newProfile);

			newPropertyMap.putAll(oldPropertyMap);

			if (mineskinURL != null && Settings.ALLOW_MINESKIN_REQUESTS.val)
			{
				Property skinProperty = propertyFromMineskinURL();

				if (skinProperty != null)
				{
					if (newPropertyMap.containsKey("textures")) { newPropertyMap.get("textures").clear(); }
					newPropertyMap.put("textures", skinProperty);
				}
				else
				{
					Utils.sendFailure(commandSource, "mocap.commands.playing.start.warning.mineskin");
				}
			}
		}
		catch (Exception ignore) {}

		recording = data.getRecording(name);
		if (recording == null) { return false; }

		fakePlayer = new FakePlayer(level, newProfile);
		PlayerActions.initFakePlayer(fakePlayer, recording, new Vector3d(offsetX, offsetY, offsetZ));
		level.addNewPlayer(fakePlayer);

		recording.preExecute(fakePlayer, blockOffset);

		packetTargets.broadcastAll(new SPlayerListItemPacket(SPlayerListItemPacket.Action.ADD_PLAYER, fakePlayer));
		packetTargets.broadcastAll(new SSpawnPlayerPacket(fakePlayer));

		packetTargets.broadcastAll(new EntityData<>(fakePlayer, EntityData.SET_SKIN_PARTS, (byte)0b01111111).getPacket());
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

			packetTargets.broadcastAll(new SDestroyEntitiesPacket(arrayOfIDs));
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
						int retVal = recording.executeNext(packetTargets, fakePlayer, blockOffset, pos++);

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
				packetTargets.broadcastAll(new SPlayerListItemPacket(SPlayerListItemPacket.Action.REMOVE_PLAYER, fakePlayer));
				list.add(fakePlayer.getId());
				break;

			case SCENE:
				for (PlayedScene playedScene : subscenes)
				{
					playedScene.removeEntities(list);
				}
				break;
		}
	}

	private @Nullable GameProfile getGameProfile(CommandSource commandSource)
	{
		String profileName = playerName;

		if (profileName == null)
		{
			if (commandSource.getEntity() instanceof ServerPlayerEntity)
			{
				profileName = ((ServerPlayerEntity)commandSource.getEntity()).getGameProfile().getName();
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
	private @Nullable Property propertyFromMineskinURL()
	{
		String mineskinID = mineskinURL.contains("/") ? mineskinURL.substring(mineskinURL.lastIndexOf('/') + 1) : mineskinURL;
		String mineskinApiURL = MINESKIN_API_URL + mineskinID;

		try
		{
			URL url = new URL(mineskinApiURL);

			URLConnection connection = url.openConnection();
			if (!(connection instanceof HttpsURLConnection)) { return null; }

			HttpsURLConnection httpsConnection = (HttpsURLConnection)connection;

			httpsConnection.setUseCaches(false);
			httpsConnection.setRequestMethod("GET");

			Scanner scanner = new Scanner(httpsConnection.getInputStream());
			String text = scanner.useDelimiter("\\A").next();

			scanner.close();
			httpsConnection.disconnect();

			String value = text.split("\"value\":\"")[1].split("\"")[0];
			String signature = text.split("\"signature\":\"")[1].split("\"")[0];

			return new Property("textures", value, signature);
		}
		catch (Exception ignore) { return null; }
	}
}
