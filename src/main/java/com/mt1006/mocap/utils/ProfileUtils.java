package com.mt1006.mocap.utils;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mt1006.mocap.MocapMod;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.CachedUserNameToIdResolver;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserNameToIdResolver;

import java.io.File;
import java.util.*;

public class ProfileUtils
{
	// Original source code: https://github.com/iChun/iChunUtil/blob/1.16/src/main/java/me/ichun/mods/ichunutil/common/entity/util/EntityHelper.java

	public static final String USERID_CACHE_FILE = "usercache.json";
	public static final Map<String, GameProfile> gameProfileCache = Collections.synchronizedMap(new HashMap<>());
	public static UserNameToIdResolver profileCache = null;
	public static MinecraftSessionService sessionService = null;

	public static GameProfile getGameProfile(MinecraftServer server, String playerName)
	{
		if (gameProfileCache.containsKey(playerName)) { return gameProfileCache.get(playerName); }

		if (profileCache == null || sessionService == null)
		{
			if (MocapMod.isDedicatedServer)
			{
				sessionService = server.services().sessionService();
				profileCache = server.services().nameToIdCache();
			}
			else
			{
				setClientProfileLookupObjects();
			}
		}

		UUID offlineUUID = UUIDUtil.createOfflinePlayerUUID(playerName);

		Optional<NameAndId> playerInfoOpt = profileCache != null ? profileCache.get(playerName) : Optional.empty();
		NameAndId playerInfo = playerInfoOpt.orElse(new NameAndId(offlineUUID, playerName));
		GameProfile gameProfile = new GameProfile(playerInfo.id(), playerInfo.name());

		Property property = Iterables.getFirst(gameProfile.properties().get("textures"), null);
		if (property == null && !playerInfo.id().equals(offlineUUID))
		{
			ProfileResult profileResult = sessionService.fetchProfile(playerInfo.id(), true);
			if (profileResult != null) { gameProfile = profileResult.profile(); }
		}

		gameProfileCache.put(gameProfile.name(), gameProfile);
		return gameProfile;
	}

	private static void setClientProfileLookupObjects()
	{
		YggdrasilAuthenticationService yggdrasilauthenticationservice = new YggdrasilAuthenticationService(Minecraft.getInstance().getProxy());
		sessionService = yggdrasilauthenticationservice.createMinecraftSessionService();
		GameProfileRepository gameprofilerepository = yggdrasilauthenticationservice.createProfileRepository();
		profileCache = new CachedUserNameToIdResolver(gameprofilerepository, new File(Minecraft.getInstance().gameDirectory, USERID_CACHE_FILE));
	}
}
