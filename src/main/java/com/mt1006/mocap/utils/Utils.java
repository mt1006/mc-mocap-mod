package com.mt1006.mocap.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.events.PlayerConnectionEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
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

	public static void sendSystemMessage(@Nullable PlayerEntity player, String component, Object... args)
	{
		if (player == null) { return; }
		player.sendMessage(getTranslatableComponent(player, component, args), Util.NIL_UUID);
	}

	public static String stringFromComponent(String component, Object... args)
	{
		return new TranslationTextComponent(component, args).getString();
	}

	public static ITextComponent getTranslatableComponent(@Nullable Entity entity, String component, Object... args)
	{
		if (supportsTranslatable(entity)) { return new TranslationTextComponent(component, args); }
		else { return new StringTextComponent(new TranslationTextComponent(component, args).getString()); }
	}

	public static ITextComponent getURLComponent(String url, String str, Object... args)
	{
		IFormattableTextComponent component = new StringTextComponent(String.format(str, args));
		return component.setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
	}

	public static CompoundNBT nbtFromString(String nbtString) throws CommandSyntaxException
	{
		return new JsonToNBT(new StringReader(nbtString)).readStruct();
	}

	private static boolean supportsTranslatable(@Nullable Entity entity)
	{
		return entity instanceof ServerPlayerEntity && PlayerConnectionEvent.players.contains((ServerPlayerEntity)entity);
	}
}
