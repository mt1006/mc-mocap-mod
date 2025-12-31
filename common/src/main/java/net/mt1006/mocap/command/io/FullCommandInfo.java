package net.mt1006.mocap.command.io;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.api.v1.modifiers.MocapPlayerAsEntity;
import net.mt1006.mocap.api.v1.modifiers.MocapPlayerSkin;
import net.mt1006.mocap.command.CommandUtils;
import net.mt1006.mocap.mocap.playing.modifiers.PlaybackModifiers;
import net.mt1006.mocap.mocap.playing.modifiers.PlayerAsEntity;
import net.mt1006.mocap.mocap.playing.modifiers.PlayerSkin;
import net.mt1006.mocap.mocap.settings.Settings;
import net.mt1006.mocap.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class FullCommandInfo implements CommandInfo
{
	public final CommandContext<CommandSourceStack> ctx;
	public final CommandSourceStack source;
	private final MinecraftServer server;
	private final ServerLevel level;
	private final @Nullable ServerPlayer sourcePlayer;
	private final @Nullable Entity sourceEntity;

	public FullCommandInfo(CommandContext<CommandSourceStack> ctx)
	{
		this.ctx = ctx;
		this.source = ctx.getSource();
		this.server = source.getServer();
		this.level = source.getLevel();
		this.sourcePlayer = source.getPlayer();
		this.sourceEntity = source.getEntity();
	}

	@Override public boolean sendSuccess(String component, Object... args)
	{
		source.sendSuccess(() -> getTranslatableComponent(component, args), false);
		return true;
	}

	@Override public boolean sendSuccessLiteral(String format, Object... args)
	{
		source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT, format, args)), false);
		return true;
	}

	@Override public boolean sendSuccessComponent(Component component)
	{
		source.sendSuccess(() -> component, false);
		return true;
	}

	@Override public boolean sendFailure(String component, Object... args)
	{
		source.sendFailure(getTranslatableComponent(component, args));
		return false;
	}

	@Override public boolean sendFailureWithTip(String component, Object... args)
	{
		source.sendFailure(getTranslatableComponent(component, args));
		if (Settings.SHOW_TIPS.val) { source.sendFailure(getTranslatableComponent(component + ".tip")); }
		return false;
	}

	@Override public boolean sendException(Exception exception, String component, Object... args)
	{
		sendFailure(component, args);
		Utils.exception(exception, Utils.stringFromComponent(component, args));
		return false;
	}

	@Override public MinecraftServer getServer()
	{
		return server;
	}

	@Override public ServerLevel getLevel()
	{
		return level;
	}

	@Override public @Nullable ServerPlayer getSourcePlayer()
	{
		return sourcePlayer;
	}

	@Override public @Nullable Entity getSourceEntity()
	{
		return sourceEntity;
	}

	@Override public String getSourceName()
	{
		return sourcePlayer != null ? sourcePlayer.getName().getString() : "+mc";
	}

	public @Nullable FullCommandInfo getFinalCommandInfo()
	{
		CommandContext<CommandSourceStack> tempCtx = ctx;
		while (true)
		{
			String command = CommandUtils.getNode(tempCtx.getNodes(), 0);
			if (command != null && (command.equals("mocap") || command.equals("mocap:mocap"))) { return new FullCommandInfo(tempCtx); }

			tempCtx = ctx.getChild();
			if (tempCtx == null) { return null; }
		}
	}

	public @Nullable String getNode(int pos)
	{
		return CommandUtils.getNode(ctx.getNodes(), pos);
	}

	public int getInteger(String name)
	{
		return IntegerArgumentType.getInteger(ctx, name);
	}

	public double getDouble(String name)
	{
		return DoubleArgumentType.getDouble(ctx, name);
	}

	public boolean getBool(String name)
	{
		return BoolArgumentType.getBool(ctx, name);
	}

	public String getString(String name)
	{
		return StringArgumentType.getString(ctx, name);
	}

	public @Nullable String getNullableString(String name)
	{
		try { return StringArgumentType.getString(ctx, name); }
		catch (Exception e) { return null; }
	}

	public @Nullable MocapPlayerSkin getPlayerSkin()
	{
		String fromPlayer = getNullableString("skin_player_name");
		if (fromPlayer != null) { return PlayerSkin.createVerified(this, MocapPlayerSkin.Source.FROM_PLAYER, fromPlayer); }

		String fromFile = getNullableString("skin_filename");
		if (fromFile != null) { return PlayerSkin.createVerified(this, MocapPlayerSkin.Source.FROM_FILE, fromFile); }

		String fromMineSkin = getNullableString("mineskin_url");
		if (fromMineSkin != null) { return PlayerSkin.createVerified(this, MocapPlayerSkin.Source.FROM_MINESKIN, fromMineSkin); }

		return PlayerSkin.DEFAULT;
	}

	public @Nullable MocapModifiers getSimpleModifiers(CommandOutput out)
	{
		String playerName = getNullableString("player_name");
		if (!PlaybackModifiers.checkIfProperPlayerName(out, playerName)) { return null; }

		MocapPlayerSkin playerSkin = getPlayerSkin();
		if (playerSkin == null) { return null; }

		MocapPlayerAsEntity playerAsEntity = PlayerAsEntity.DISABLED;

		try
		{
			String playerAsEntityId = ResourceArgument.getEntityType(ctx, "entity").key().location().toString();

			Tag tag;
			try { tag = NbtTagArgument.getNbtTag(ctx, "nbt"); }
			catch (Exception e) { tag = null; }
			CompoundTag nbt = (tag instanceof CompoundTag) ? (CompoundTag)tag : null;

			playerAsEntity = new PlayerAsEntity(playerAsEntityId, nbt != null ? nbt.toString() : null);
		}
		catch (Exception ignore) {}

		return PlaybackModifiers.DEFAULT.withPlayerName(playerName).withPlayerSkin(playerSkin).withPlayerAsEntity(playerAsEntity);
	}
}
