package com.mt1006.mocap.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.events.PlayerConnectionEvent;
import net.minecraft.command.CommandSource;
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

	public static byte[] floatToByteArray(float val)
	{
		int bits = Float.floatToIntBits(val);
		return new byte[] { (byte)(bits >> 24), (byte)(bits >> 16), (byte)(bits >> 8), (byte)bits };
	}

	public static float byteArrayToFloat(byte[] bytes)
	{
		int bits = (((int)bytes[0] & 0xFF) << 24) | (((int)bytes[1] & 0xFF) << 16) | (((int)bytes[2] & 0xFF) << 8) | ((int)bytes[3] & 0xFF);
		return Float.intBitsToFloat(bits);
	}

	public static byte[] doubleToByteArray(double val)
	{
		long bits = Double.doubleToLongBits(val);
		return new byte[] { (byte)(bits >> 56), (byte)(bits >> 48), (byte)(bits >> 40), (byte)(bits >> 32),
				(byte)(bits >> 24), (byte)(bits >> 16), (byte)(bits >> 8), (byte)bits };
	}

	public static double byteArrayToDouble(byte[] bytes)
	{
		long bits = (((long)bytes[0] & 0xFF) << 56) | (((long)bytes[1] & 0xFF) << 48) | (((long)bytes[2] & 0xFF) << 40) | (((long)bytes[3] & 0xFF) << 32) |
				(((long)bytes[4] & 0xFF) << 24) | (((long)bytes[5] & 0xFF) << 16) | (((long)bytes[6] & 0xFF) << 8) | ((long)bytes[7] & 0xFF);
		return Double.longBitsToDouble(bits);
	}

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

	public static void sendSuccess(CommandSource commandSource, String component, Object... args)
	{
		commandSource.sendSuccess(getTranslatableComponent(commandSource.getEntity(), component, args), false);
	}

	public static void sendSuccessLiteral(CommandSource commandSource, String format, Object... args)
	{
		commandSource.sendSuccess(new StringTextComponent(String.format(format, args)), false);
	}

	public static void sendSuccessComponent(CommandSource commandSource, ITextComponent component)
	{
		commandSource.sendSuccess(component, false);
	}

	public static void sendFailure(CommandSource commandSource, String component, Object... args)
	{
		commandSource.sendFailure(getTranslatableComponent(commandSource.getEntity(), component, args));
	}

	public static void sendException(Exception exception, CommandSource commandSource, String component, Object... args)
	{
		sendFailure(commandSource, component, args);
		exception(exception, stringFromComponent(component, args));
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
