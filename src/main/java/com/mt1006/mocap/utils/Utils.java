package com.mt1006.mocap.utils;

import com.mt1006.mocap.events.PlayerConnectionEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class Utils
{
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

	public static void sendSuccess(CommandSourceStack commandSource, String component, Object... args)
	{
		commandSource.sendSuccess(() -> getTranslatableComponent(commandSource.getEntity(), component, args), false);
	}

	public static void sendSuccessLiteral(CommandSourceStack commandSource, String format, Object... args)
	{
		commandSource.sendSuccess(() -> Component.literal(String.format(format, args)), false);
	}

	public static void sendSuccessComponent(CommandSourceStack commandSource, Component component)
	{
		commandSource.sendSuccess(() -> component, false);
	}

	public static void sendFailure(CommandSourceStack commandSource, String component, Object... args)
	{
		commandSource.sendFailure(getTranslatableComponent(commandSource.getEntity(), component, args));
	}

	public static void sendSystemMessage(ServerPlayer player, String component, Object... args)
	{
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
		return component.setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
	}

	private static boolean supportsTranslatable(@Nullable Entity entity)
	{
		if (entity == null) { return false; }
		return entity instanceof Player && PlayerConnectionEvent.players.contains((Player)entity);
	}
}
