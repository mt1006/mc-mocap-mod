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
import net.mt1006.mocap.api.v1.controller.config.MocapPlayerNameHandling;
import net.mt1006.mocap.mocap.settings.Settings;
import org.jetbrains.annotations.Nullable;

import java.net.Proxy;
import java.util.Locale;
import java.util.UUID;

public class ProfileUtils
{
	private static final MinecraftClient client = new MinecraftClient(null, Proxy.NO_PROXY);
	private static final Cache<String, Profile> cache = new Cache<>("profiles - case sensitive", 4096);
	private static final Cache<String, Profile> cacheInsensitive = new Cache<>("profiles - case insensitive", 4096);

	public static Profile getProfile(MinecraftServer server, MocapPlayerNameHandling mode, String name, boolean withSkin)
	{
		if (mode == MocapPlayerNameHandling.DISABLE_LOADING_PROFILES) { return Profile.withoutSkin(name); }

		Profile cachedProfile = getFromCache(mode, name, withSkin);
		if (cachedProfile != null) { return cachedProfile; }
		if (!StringUtil.isValidPlayerName(name)) { return cacheAndReturn(Profile.withoutSkin(name)); }

		Services services = server.services();
		NameAndIdResult nameAndId = fetchNameAndId(services, name);
		if (nameAndId.val == null)
		{
			Profile	profile	= nameAndId.rateLimited ? Profile.rateLimited(name) : Profile.withoutSkin(name);
			return cacheAndReturn(profile);
		}

		if (mode == MocapPlayerNameHandling.MATCH_EXACT_NAME && !name.equals(nameAndId.val.name()))
		{
			cacheAndReturn(Profile.fromPartialFetch(nameAndId.val));

			Profile exactNameProfile = Profile.withoutSkin(name);
			cache.put(name, exactNameProfile); // we don't want to put it into cacheInsensitive (what cacheAndReturn does)
			return exactNameProfile;
		}

		Profile newProfile = cacheAndReturn(withSkin
				? fetchFullProfile(services, nameAndId.val)
				: Profile.fromPartialFetch(nameAndId.val));
		return mode == MocapPlayerNameHandling.IGNORE_CASING ? newProfile.withName(name) : newProfile;
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
		cacheInsensitive.clear();
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

	public static @Nullable Profile getFromCache(MocapPlayerNameHandling mode, String name, boolean withSkin)
	{
		Profile profile = switch (mode)
		{
			case IGNORE_CASING, IGNORE_AND_REPLACE_CASING -> cacheInsensitive.get(name.toLowerCase(Locale.ROOT));
			case MATCH_EXACT_NAME -> cache.get(name);
			case DISABLE_LOADING_PROFILES -> throw new IllegalStateException("Unexpected value: " + mode);
		};

		if (profile == null || (profile.partialFetch && withSkin)) { return null; }
		if (profile.skin != null && !withSkin) { profile = profile.withoutSkin(); }

		return mode == MocapPlayerNameHandling.IGNORE_CASING ? profile.withName(name) : profile;
	}

	public static Profile cacheAndReturn(Profile profile)
	{
		if (!profile.rateLimited)
		{
			Profile prevVal = cache.get(profile.name);
			if (prevVal == null || prevVal.partialFetch) { cache.put(profile.name, profile); }

			String lowercaseName = profile.name.toLowerCase(Locale.ROOT);
			Profile prevInsensitiveVal = cacheInsensitive.get(lowercaseName);
			if (prevInsensitiveVal == null || prevInsensitiveVal.partialFetch) { cacheInsensitive.put(lowercaseName, profile); }
		}
		return profile;
	}

	private record NameAndIdResult(@Nullable NameAndId val, boolean rateLimited)
	{
		public NameAndIdResult(@Nullable NameAndId val)
		{
			this(val, false);
		}
	}

	public static class Profile
	{
		public final String name;
		public final @Nullable Property skin;
		public final boolean rateLimited;
		public final boolean partialFetch;

		public static Profile withoutSkin(String name)
		{
			return new Profile(name, null, false, false);
		}

		public static Profile withSkin(String name, @Nullable Property skin)
		{
			return new Profile(name, skin, false, false);
		}

		private static Profile rateLimited(String name)
		{
			return new Profile(name, null, true, false);
		}

		private static Profile fromPartialFetch(NameAndId nameAndId)
		{
			return new Profile(nameAndId.name(), null, false, true);
		}

		private Profile(String name, @Nullable Property skin, boolean rateLimited, boolean partialFetch)
		{
			this.name = name;
			this.skin = skin;
			this.rateLimited = rateLimited;
			this.partialFetch = partialFetch;
		}

		public Profile withName(String newName)
		{
			return new Profile(newName, skin, rateLimited, partialFetch);
		}

		public Profile withoutSkin()
		{
			return new Profile(name, null, rateLimited, partialFetch);
		}
	}
}
