package com.mt1006.mocap.utils;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;
import java.util.*;

public class ProfileUtils
{
	// Original source code: https://github.com/iChun/iChunUtil/blob/1.16/src/main/java/me/ichun/mods/ichunutil/common/entity/util/EntityHelper.java

	public static Map<String, GameProfile> gameProfileCache = Collections.synchronizedMap(new HashMap<>());
	public static GameProfileCache profileCache;
	public static MinecraftSessionService sessionService;

	public static GameProfile getGameProfile(String playerName)
	{
		if (playerName != null && gameProfileCache.containsKey(playerName))
		{
			return gameProfileCache.get(playerName);
		}

		if (profileCache == null || sessionService == null)
		{
			if (FMLEnvironment.dist.isDedicatedServer())
			{
				sessionService = ServerLifecycleHooks.getCurrentServer().getSessionService();
				profileCache = ServerLifecycleHooks.getCurrentServer().getProfileCache();
			}
			else
			{
				setClientProfileLookupObjects();
			}
		}

		Optional<GameProfile> optional = profileCache.get(playerName);
		GameProfile gameprofile = optional.orElseGet(() -> sessionService.fillProfileProperties(new GameProfile(null, playerName), true));

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
