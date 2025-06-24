package net.mt1006.mocap.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.mt1006.mocap.command.CommandSuggestions;
import net.mt1006.mocap.command.CommandUtils;
import net.mt1006.mocap.command.CommandsContext;
import net.mt1006.mocap.command.io.CommandInfo;
import net.mt1006.mocap.command.io.CommandOutput;
import net.mt1006.mocap.events.PlayerConnectionEvent;
import net.mt1006.mocap.mocap.playing.skins.CustomServerSkinManager;
import net.mt1006.mocap.network.MocapPacketS2C;

public class MiscCommand
{
	public static LiteralArgumentBuilder<CommandSourceStack> getArgumentBuilder()
	{
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands.literal("misc");

		commandBuilder.then(Commands.literal("sync").
			then(Commands.literal("enable").executes(CommandUtils.command(MiscCommand::syncEnable))).
			then(Commands.literal("disable").executes(CommandUtils.command(MiscCommand::syncDisable))));
		commandBuilder.then(Commands.literal("clear_cache").executes(CommandUtils.command(MiscCommand::clearCache)));
		//TODO: add "api"

		return commandBuilder;
	}

	private static boolean syncEnable(CommandInfo commandInfo)
	{
		if (commandInfo.getSourcePlayer() == null)
		{
			commandInfo.sendFailure("failure.resolve_player");
			return false;
		}

		CommandsContext ctx = CommandsContext.get(commandInfo.getSourcePlayer());
		commandInfo.sendSuccess(ctx.setSync(true) ? "misc.sync.enable.not_changed" : "misc.sync.enable.changed");
		return true;
	}

	private static boolean syncDisable(CommandInfo commandInfo)
	{
		if (commandInfo.getSourcePlayer() == null)
		{
			commandInfo.sendFailure("failure.resolve_player");
			return false;
		}

		CommandsContext ctx = CommandsContext.get(commandInfo.getSourcePlayer());
		commandInfo.sendSuccess(ctx.setSync(false) ? "misc.sync.disable.changed" : "misc.sync.disable.not_changed");
		return true;
	}

	private static boolean clearCache(CommandOutput commandOutput)
	{
		CommandSuggestions.clearCache();
		CustomServerSkinManager.clearCache();
		PlayerConnectionEvent.players.forEach(MocapPacketS2C::sendClearCache);

		commandOutput.sendSuccess("misc.clear_cache.cleared");
		return true;
	}
}
