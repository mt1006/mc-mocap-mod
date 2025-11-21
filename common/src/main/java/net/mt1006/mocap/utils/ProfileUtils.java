package net.mt1006.mocap.utils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.exceptions.MinecraftClientHttpException;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import com.mojang.authlib.yggdrasil.response.NameAndId;
import com.mojang.util.UndashedUuid;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.util.StringUtil;
import net.mt1006.mocap.mocap.settings.Settings;
import org.jetbrains.annotations.Nullable;

import java.net.Proxy;
import java.util.*;

public class ProfileUtils
{
	private static final MinecraftClient client = new MinecraftClient(null, Proxy.NO_PROXY);
	private static final Map<String, Profile> cache = Collections.synchronizedMap(new HashMap<>());

	public static Profile getProfile(MinecraftServer server, String name, boolean fetchSkin)
	{
		//TODO: properly use fetchSkin
		if (cache.containsKey(name.toLowerCase(Locale.ROOT))) { return cache.get(name.toLowerCase(Locale.ROOT)); }
		if (!StringUtil.isValidPlayerName(name)) { return cacheAndReturn(Profile.withoutSkin(name)); }

		Services services = server.services();

		NameAndIdResult nameAndId = fetchNameAndId(services, name);
		if (nameAndId.val == null)
		{
			Profile	profile	= nameAndId.rateLimited ? Profile.rateLimited(name) : Profile.withoutSkin(name);
			return cacheAndReturn(profile);
		}

		return cacheAndReturn(fetchFullProfile(services, nameAndId.val));
	}

	public static GameProfile createGameProfile(String name, @Nullable Property skin, @Nullable Property customSkin)
	{
		Multimap<String, Property> multimap = HashMultimap.create();
		if (skin != null) { multimap.put(skin.name(), skin); }
		if (customSkin != null) { multimap.put(customSkin.name(), customSkin); }

		return new GameProfile(UUID.randomUUID(), name, new PropertyMap(multimap));
	}

	public static void clearCache()
	{
		cache.clear();
	}

	private static NameAndIdResult fetchNameAndId(Services services, String playerName)
	{
		if (Settings.USE_AUTHLIB_SERVICES.val)
		{
			return new NameAndIdResult(services.profileRepository().findProfileByName(playerName).orElse(null));
		}

		try
		{
			return new NameAndIdResult(client.get(
					HttpAuthenticationService.constantURL("https://api.mojang.com/users/profiles/minecraft/" + playerName.toLowerCase(Locale.ROOT)),
					NameAndId.class));
		}
		catch (MinecraftClientException e)
		{
			boolean rateLimited = (e instanceof MinecraftClientHttpException httpException && httpException.getStatus() == 429);
			return new NameAndIdResult(null, rateLimited);
		}
	}

	private static Profile fetchFullProfile(Services services, NameAndId nameAndId)
	{
		if (Settings.USE_AUTHLIB_SERVICES.val)
		{
			ProfileResult result = services.sessionService().fetchProfile(nameAndId.id(), true);

			return result != null
					? Profile.withSkin(nameAndId.name(), services.sessionService().getPackedTextures(result.profile()))
					: Profile.withoutSkin(nameAndId.name());
		}

		try
		{
			MinecraftProfilePropertiesResponse response = client.get(
					HttpAuthenticationService.constantURL("https://sessionserver.mojang.com/session/minecraft/profile/"
							+ UndashedUuid.toString(nameAndId.id()) + "?unsigned=false"),
					MinecraftProfilePropertiesResponse.class);

			return response != null
					? Profile.withSkin(nameAndId.name(), Iterables.getFirst(response.properties().get("textures"), null))
					: Profile.withoutSkin(nameAndId.name());

		}
		catch (MinecraftClientException e)
		{
			boolean rateLimited = (e instanceof MinecraftClientHttpException httpException && httpException.getStatus() == 429);
			return rateLimited ? Profile.rateLimited(nameAndId.name()) : Profile.withoutSkin(nameAndId.name());
		}
	}

	public static Profile cacheAndReturn(Profile profile)
	{
		if (!profile.rateLimited) { cache.put(profile.name.toLowerCase(Locale.ROOT), profile); }
		return profile;
	}

	//TODO: remove after pushing to repo
	/*private static @Nullable MinecraftClient getMinecraftApiClient(GameProfileRepository profileRepository)
	{
		if (apiClient != null) { return apiClient; }

		if (!(profileRepository instanceof YggdrasilGameProfileRepository))
		{
			printApiClientFieldError("not supported implementation of profile repository");
			return null;
		}
		Field[] fields = YggdrasilGameProfileRepository.class.getDeclaredFields();

		for (Field field : fields)
		{
			if (MinecraftClient.class.isAssignableFrom(field.getType()))
			{
				field.setAccessible(true);
				try
				{
					apiClient = (MinecraftClient)field.get(profileRepository);
					return apiClient;
				}
				catch (IllegalAccessException e)
				{
					printApiClientFieldError("failed to get value");
					return null;
				}
			}
		}

		printApiClientFieldError("field not found");
		return null;
	}

	private static void printApiClientFieldError(String message)
	{
		if (!apiClientFieldFailed)
		{
			MocapMod.LOGGER.error("Failed to retrieve MinecraftClient instance - {}!", message);
			apiClientFieldFailed = true;
		}
	}

	private static GameProfile createOffline(String playerName)
	{
		return new GameProfile(UUIDUtil.createOfflinePlayerUUID(playerName), playerName);
	}*/

	public static class NameAndIdResult
	{
		public final @Nullable NameAndId val;
		public final boolean rateLimited;

		public NameAndIdResult(@Nullable NameAndId val)
		{
			this(val, false);
		}

		public NameAndIdResult(@Nullable NameAndId val, boolean rateLimited)
		{
			this.val = val;
			this.rateLimited = rateLimited;
		}
	}

	public static class Profile
	{
		public final String name;
		public final @Nullable Property skin;
		public final boolean rateLimited;

		public static Profile withoutSkin(String name)
		{
			return new Profile(name, null, false);
		}

		public static Profile withSkin(String name, @Nullable Property skin)
		{
			return new Profile(name, skin, false);
		}

		public static Profile rateLimited(String name)
		{
			return new Profile(name, null, true);
		}

		private Profile(String name, @Nullable Property skin, boolean rateLimited)
		{
			this.name = name;
			this.skin = skin;
			this.rateLimited = rateLimited;
		}
	}
}
