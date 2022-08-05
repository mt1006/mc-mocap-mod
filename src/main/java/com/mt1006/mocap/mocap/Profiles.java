package com.mt1006.mocap.mocap;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mt1006.mocap.DedicatedServerInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;

import java.io.File;
import java.util.*;

public class Profiles
{
	// Original source code: https://github.com/iChun/iChunUtil/blob/1.16/src/main/java/me/ichun/mods/ichunutil/common/entity/util/EntityHelper.java

	public static Map<String, GameProfile> gameProfileCache = Collections.synchronizedMap(new HashMap<>());
	public static GameProfileCache profileCache;
	public static MinecraftSessionService sessionService;

	public static GameProfile getGameProfile(MinecraftServer server, String playerName)
	{
		if (playerName != null && gameProfileCache.containsKey(playerName))
		{
			return gameProfileCache.get(playerName);
		}

		if (profileCache == null || sessionService == null)
		{
			if (DedicatedServerInitializer.isDedicatedServer)
			{
				sessionService = server.getSessionService();
				profileCache = server.getProfileCache();
			}
			else
			{
				setClientProfileLookupObjects();
			}
		}

		GameProfile gameprofile = profileCache.get(playerName);
		if (gameprofile == null)
		{
			gameprofile = sessionService.fillProfileProperties(new GameProfile(null, playerName), true);
		}

		Property property = Iterables.getFirst(gameprofile.getProperties().get("textures"), null);
		if (property == null)
		{
			gameprofile = sessionService.fillProfileProperties(gameprofile, true);
		}

		gameProfileCache.put(gameprofile.getName(), gameprofile);
		return gameprofile;
	}

	private static void setClientProfileLookupObjects()
	{
		YggdrasilAuthenticationService yggdrasilauthenticationservice = new YggdrasilAuthenticationService(Minecraft.getInstance().getProxy(), UUID.randomUUID().toString());
		sessionService = yggdrasilauthenticationservice.createMinecraftSessionService();
		GameProfileRepository gameprofilerepository = yggdrasilauthenticationservice.createProfileRepository();
		profileCache = new GameProfileCache(gameprofilerepository, new File(Minecraft.getInstance().gameDirectory, MinecraftServer.USERID_CACHE_FILE.getName()));
	}
}
