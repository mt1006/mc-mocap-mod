package com.mt1006.mocap.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.events.PlayerConnectionEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;

public class Utils
{
	public static final String NULL_STR = "[null]";

	public static @Nullable String toNullableStr(String str)
	{
		return !str.equals(NULL_STR) ? str : null;
	}

	public static String toNotNullStr(@Nullable String str)
	{
		return str != null ? str : NULL_STR;
	}

	public static void exception(Exception exception, String str)
	{
		MocapMod.LOGGER.error(str);
		exception.printStackTrace();
	}

	public static void sendSystemMessage(@Nullable Player player, String component, Object... args)
	{
		if (player == null) { return; }
		player.sendSystemMessage(getTranslatableComponent(player, component, args));
	}

	public static String stringFromComponent(String component, Object... args)
	{
		return Component.translatable(component, args).getString();
	}

	public static Component getTranslatableComponent(@Nullable Entity entity, String component, Object... args)
	{
		if (supportsTranslatable(entity)) { return Component.translatable(component, args); }
		else { return Component.literal(Component.translatable(component, args).getString()); }
	}

	public static Component getURLComponent(String url, String str, Object... args)
	{
		MutableComponent component = Component.literal(String.format(str, args));

		URI uri;
		try { uri = new URI(url); }
		catch (URISyntaxException e) { uri = null; }

		return uri != null ? component.setStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(uri))) : component;
	}

	public static CompoundTag nbtFromString(String nbtString) throws CommandSyntaxException
	{
		return TagParser.parseCompoundFully(nbtString);
	}

	private static boolean supportsTranslatable(@Nullable Entity entity)
	{
		return entity instanceof ServerPlayer && PlayerConnectionEvent.players.contains((ServerPlayer)entity);
	}
}
