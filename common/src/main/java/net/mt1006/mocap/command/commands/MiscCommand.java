package net.mt1006.mocap.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.mt1006.mocap.api.impl.extenstion.Extensions;
import net.mt1006.mocap.api.impl.extenstion.MocapExtensionImpl;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.command.CommandSuggestions;
import net.mt1006.mocap.command.CommandUtils;
import net.mt1006.mocap.command.CommandsContext;
import net.mt1006.mocap.command.converter.AlphaConverter;
import net.mt1006.mocap.events.PlayerConnectionEvent;
import net.mt1006.mocap.mocap.playing.skins.CustomServerSkinManager;
import net.mt1006.mocap.network.MocapPacketS2C;
import net.mt1006.mocap.utils.ProfileUtils;

import java.util.Collection;

public class MiscCommand
{
	public static LiteralArgumentBuilder<CommandSourceStack> getArgumentBuilder()
	{
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands.literal("misc");

		commandBuilder.then(Commands.literal("sync").
			then(Commands.literal("enable").executes(CommandUtils.command(MiscCommand::syncEnable))).
			then(Commands.literal("disable").executes(CommandUtils.command(MiscCommand::syncDisable))));
		commandBuilder.then(Commands.literal("clear_cache").executes(CommandUtils.command(MiscCommand::clearCache)));
		commandBuilder.then(Commands.literal("refresh_suggestions").executes(CommandUtils.command(MiscCommand::refreshSuggestions)));
		commandBuilder.then(Commands.literal("extensions").executes(CommandUtils.command(MiscCommand::extensions)));

		//TODO: [CONVERTER] remove
		commandBuilder.then(Commands.literal("convert").then(CommandUtils.withInputArgument(AlphaConverter::command, CommandSuggestions::recording, "name")));

		return commandBuilder;
	}

	private static boolean syncEnable(CommandInfo info)
	{
		if (info.getSourcePlayer() == null) { return info.sendFailure("failure.resolve_player"); }

		CommandsContext ctx = CommandsContext.get(info.getSourcePlayer());
		return info.sendSuccess(ctx.setSync(true) ? "misc.sync.enable.not_changed" : "misc.sync.enable.changed");
	}

	private static boolean syncDisable(CommandInfo info)
	{
		if (info.getSourcePlayer() == null) { return info.sendFailure("failure.resolve_player"); }

		CommandsContext ctx = CommandsContext.get(info.getSourcePlayer());
		return info.sendSuccess(ctx.setSync(false) ? "misc.sync.disable.changed" : "misc.sync.disable.not_changed");
	}

	private static boolean clearCache(CommandOutput out)
	{
		CommandSuggestions.clearCache();
		CustomServerSkinManager.clearCache();
		PlayerConnectionEvent.players.forEach(MocapPacketS2C::sendClearCache);
		ProfileUtils.clearCache();

		return out.sendSuccess("misc.clear_cache.success");
	}

	private static boolean refreshSuggestions(CommandOutput out)
	{
		CommandSuggestions.refresh();
		return out.sendSuccess("misc.refresh_suggestions.success");
	}

	private static boolean extensions(CommandOutput out)
	{
		Collection<MocapExtensionImpl> extensions = Extensions.getExtensions();
		if (extensions.isEmpty())
		{
			out.sendSuccess("misc.extensions.no_extensions");
		}
		else
		{
			out.sendSuccess("misc.extensions.list");
			for (MocapExtensionImpl ext : extensions)
			{
				int version = ext.getVersion();
				if (version >= 0) { out.sendSuccess("misc.extensions.info", ext.getId(), version); }
				else { out.sendSuccess("misc.extensions.info.experimental", ext.getId(), -version); }
			}
		}
		return true;
	}
}
