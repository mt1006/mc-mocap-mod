package com.mt1006.mocap.utils;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mt1006.mocap.IsDedicatedServer;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileUtils
{
	// Original source code: https://github.com/iChun/iChunUtil/blob/1.16/src/main/java/me/ichun/mods/ichunutil/common/entity/util/EntityHelper.java

	public static final String USERID_CACHE_FILE = "usercache.json";
	public static final Map<String, GameProfile> gameProfileCache = Collections.synchronizedMap(new HashMap<>());
	public static PlayerProfileCache profileCache = null;
	public static MinecraftSessionService sessionService = null;

	public static GameProfile getGameProfile(MinecraftServer server, String playerName)
	{
		if (playerName != null && gameProfileCache.containsKey(playerName))
		{
			return gameProfileCache.get(playerName);
		}

		if (profileCache == null || sessionService == null)
		{
			if (IsDedicatedServer.isDedicatedServer)
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
		YggdrasilAuthenticationService yggdrasilauthenticationservice =
				new YggdrasilAuthenticationService(Minecraft.getInstance().getProxy(), UUID.randomUUID().toString());
		sessionService = yggdrasilauthenticationservice.createMinecraftSessionService();
		GameProfileRepository gameprofilerepository = yggdrasilauthenticationservice.createProfileRepository();
		profileCache = new PlayerProfileCache(gameprofilerepository, new File(Minecraft.getInstance().gameDirectory, USERID_CACHE_FILE));
	}
}
