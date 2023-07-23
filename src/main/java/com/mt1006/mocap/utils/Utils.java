package com.mt1006.mocap.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.events.PlayerConnectionEvent;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

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
		player.sendMessage(getTranslatableComponent(player, component, args), Util.NIL_UUID);
	}

	public static String stringFromComponent(String component, Object... args)
	{
		return new TranslatableComponent(component, args).getString();
	}

	public static Component getTranslatableComponent(@Nullable Entity entity, String component, Object... args)
	{
		if (supportsTranslatable(entity)) { return new TranslatableComponent(component, args); }
		else { return new TextComponent(new TranslatableComponent(component, args).getString()); }
	}

	public static Component getURLComponent(String url, String str, Object... args)
	{
		MutableComponent component = new TextComponent(String.format(str, args));
		return component.setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
	}

	public static CompoundTag nbtFromString(String nbtString) throws CommandSyntaxException
	{
		return new TagParser(new StringReader(nbtString)).readStruct();
	}

	private static boolean supportsTranslatable(@Nullable Entity entity)
	{
		return entity instanceof ServerPlayer && PlayerConnectionEvent.players.contains((ServerPlayer)entity);
	}
}
