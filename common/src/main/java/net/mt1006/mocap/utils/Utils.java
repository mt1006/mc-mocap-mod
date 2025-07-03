package net.mt1006.mocap.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.events.PlayerConnectionEvent;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;

public class Utils
{
	public static void exception(Exception exception, String str)
	{
		MocapMod.LOGGER.error(str);
		exception.printStackTrace();
	}

	public static void sendMessage(@Nullable Player player, String component, Object... args)
	{
		if (player == null) { return; }
		player.displayClientMessage(getTranslatableComponent(player, component, args), false);
	}

	public static void sendComponent(@Nullable Player player, Component component)
	{
		if (player == null) { return; }
		player.displayClientMessage(component, false);
	}

	public static String stringFromComponent(String component, Object... args)
	{
		return Component.translatable("mocap." + component, args).getString();
	}

	public static MutableComponent getTranslatableComponent(@Nullable Entity entity, String component, Object... args)
	{
		String key = "mocap." + component;
		return supportsTranslatable(entity)
				? Component.translatable(key, args)
				: Component.literal(Component.translatable(key, args).getString());
	}

	public static MutableComponent getSuggestCommandComponent(String toSuggest, MutableComponent component)
	{
		return component.setStyle(Style.EMPTY.withClickEvent(new ClickEvent.SuggestCommand(toSuggest)));
	}

	public static MutableComponent getOpenUrlComponent(String url, MutableComponent component)
	{
		try { return component.setStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(new URI(url)))); }
		catch (URISyntaxException e) { return component; }
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
