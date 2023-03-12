package com.mt1006.mocap.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.mocap.commands.Playing;
import com.mt1006.mocap.mocap.commands.Recording;
import com.mt1006.mocap.mocap.commands.Scenes;
import com.mt1006.mocap.mocap.commands.Settings;
import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.mocap.files.SceneFile;
import com.mt1006.mocap.mocap.playing.SceneInfo;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Collection;

public class MocapCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("mocap").requires(source -> source.hasPermission(2)).
				then(Commands.literal("recording").
					then(Commands.literal("start").executes(MocapCommand::recordingStart).
						then(Commands.argument("player", GameProfileArgument.gameProfile()).executes(MocapCommand::recordingStart))).
					then(Commands.literal("stop").executes(Recording::stop)).
					then(Commands.literal("save").
						then(Commands.argument("name", StringArgumentType.string()).executes(MocapCommand::recordingSave))).
					then(Commands.literal("remove").
						then(Commands.argument("name", StringArgumentType.string()).executes(MocapCommand::recordingRemove))).
					then(Commands.literal("list").executes(Recording::list)).
					then(Commands.literal("state").executes(Recording::state))).
				then(Commands.literal("scenes").
					then(Commands.literal("add").
						then(Commands.argument("name", StringArgumentType.string()).executes(MocapCommand::scenesAdd))).
					then(Commands.literal("remove").
						then(Commands.argument("name", StringArgumentType.string()).executes(MocapCommand::scenesRemove))).
					then(Commands.literal("addTo").
						then(Commands.argument("sceneName", StringArgumentType.string()).
						then(Commands.argument("toAdd", StringArgumentType.string()).executes(MocapCommand::scenesAddTo).
						then(Commands.argument("startDelay", DoubleArgumentType.doubleArg(0.0)).executes(MocapCommand::scenesAddTo).
						then(Commands.argument("offsetX", DoubleArgumentType.doubleArg()).executes(MocapCommand::scenesAddTo).
						then(Commands.argument("offsetY", DoubleArgumentType.doubleArg()).executes(MocapCommand::scenesAddTo).
						then(Commands.argument("offsetZ", DoubleArgumentType.doubleArg()).executes(MocapCommand::scenesAddTo).
						then(Commands.argument("playerName", StringArgumentType.string()).executes(MocapCommand::scenesAddTo).
						then(Commands.argument("mineskinURL", StringArgumentType.greedyString()).executes(MocapCommand::scenesAddTo)))))))))).
					/*then(Commands.literal("modify").
						then(Commands.literal("recordingName").
							then(Commands.argument("recordingName", StringArgumentType.string()).executes(MocapCommand::scenesModify))).
						then(Commands.literal("startDelay").
							then(Commands.argument("startDelay", DoubleArgumentType.doubleArg(0.0)).executes(MocapCommand::scenesModify))).
						then(Commands.literal("positionOffset").
							then(Commands.argument("offsetX", DoubleArgumentType.doubleArg())).
							then(Commands.argument("offsetY", DoubleArgumentType.doubleArg())).
							then(Commands.argument("offsetZ", DoubleArgumentType.doubleArg()).executes(MocapCommand::scenesModify))).
						then(Commands.literal("playerName").
							then(Commands.argument("playerName", StringArgumentType.string()).executes(MocapCommand::scenesModify)))).*/
					then(Commands.literal("removeFrom").
						then(Commands.argument("sceneName", StringArgumentType.string()).
						then(Commands.argument("toRemove", IntegerArgumentType.integer()).executes(MocapCommand::scenesRemoveFrom)))).
					then(Commands.literal("listElements").
						then(Commands.argument("name", StringArgumentType.string()).executes(MocapCommand::scenesListElements))).
					then(Commands.literal("list").executes(Scenes::list))).
				then(Commands.literal("playing").
					then(Commands.literal("start").
						then(Commands.argument("name", StringArgumentType.string()).executes(MocapCommand::playingStart).
						then(Commands.argument("playerName", StringArgumentType.string()).executes(MocapCommand::playingStart).
						then(Commands.argument("mineskinURL", StringArgumentType.greedyString()).executes(MocapCommand::playingStart))))).
					then(Commands.literal("stop").
						then(Commands.argument("id", IntegerArgumentType.integer()).executes(MocapCommand::playingStop))).
					then(Commands.literal("stopAll").executes(Playing::stopAll)).
					then(Commands.literal("list").executes(Playing::list))).
				then(Commands.literal("settings").executes(Settings::list).
					then(Commands.literal("playingSpeed").executes(Settings::info).
						then(Commands.argument("newValue", DoubleArgumentType.doubleArg(0.0)).executes(Settings::set))).
					then(Commands.literal("recordingSync").executes(Settings::info).
						then(Commands.argument("newValue", BoolArgumentType.bool()).executes(Settings::set))).
					then(Commands.literal("playBlockActions").executes(Settings::info).
						then(Commands.argument("newValue", BoolArgumentType.bool()).executes(Settings::set))).
					then(Commands.literal("setBlockStates").executes(Settings::info).
						then(Commands.argument("newValue", BoolArgumentType.bool()).executes(Settings::set))).
					then(Commands.literal("allowMineskinRequests").executes(Settings::info).
						then(Commands.argument("newValue", BoolArgumentType.bool()).executes(Settings::set)))).
				then(Commands.literal("info").executes(MocapCommand::info)).
				then(Commands.literal("help").executes(MocapCommand::help)));
	}

	private static int recordingStart(CommandContext<CommandSource> ctx)
	{
		ServerPlayerEntity serverPlayer;

		try
		{
			Collection<GameProfile> gameProfiles = GameProfileArgument.getGameProfiles(ctx, "player");

			if (gameProfiles.size() != 1)
			{
				Utils.sendFailure(ctx.getSource(), "mocap.recording.start.player_not_found");
				return 0;
			}

			String nickname = gameProfiles.iterator().next().getName();
			serverPlayer = ctx.getSource().getServer().getPlayerList().getPlayerByName(nickname);

			if(serverPlayer == null)
			{
				Utils.sendFailure(ctx.getSource(), "mocap.recording.start.player_not_found");
				return 0;
			}
		}
		catch (Exception exception)
		{
			Entity entity = ctx.getSource().getEntity();

			if (!(entity instanceof ServerPlayerEntity))
			{
				Utils.sendFailure(ctx.getSource(), "mocap.recording.start.player_not_specified");
				Utils.sendFailure(ctx.getSource(), "mocap.recording.start.player_not_specified.tip");
				return 0;
			}

			serverPlayer = (ServerPlayerEntity)entity;
		}

		return Recording.start(ctx.getSource(), serverPlayer);
	}

	private static int recordingSave(CommandContext<CommandSource> ctx)
	{
		try
		{
			String name = StringArgumentType.getString(ctx, "name");
			return Recording.save(ctx.getSource(), name);
		}
		catch (Exception exception)
		{
			Utils.sendFailure(ctx.getSource(), "mocap.error.unable_to_get_argument");
			return 0;
		}
	}

	private static int recordingRemove(CommandContext<CommandSource> ctx)
	{
		try
		{
			String name = StringArgumentType.getString(ctx, "name");
			if (!RecordingFile.remove(ctx.getSource(), name)) { return 0; }
		}
		catch (Exception exception)
		{
			Utils.sendFailure(ctx.getSource(), "mocap.error.unable_to_get_argument");
			return 0;
		}

		return 1;
	}

	private static int scenesAdd(CommandContext<CommandSource> ctx)
	{
		try
		{
			String name = StringArgumentType.getString(ctx, "name");
			if (!SceneFile.add(ctx.getSource(), name)) { return 0; }
		}
		catch (Exception exception)
		{
			Utils.sendFailure(ctx.getSource(), "mocap.error.unable_to_get_argument");
			return 0;
		}

		return 1;
	}

	private static int scenesRemove(CommandContext<CommandSource> ctx)
	{
		try
		{
			String name = StringArgumentType.getString(ctx, "name");
			if (!SceneFile.remove(ctx.getSource(), name)) { return 0; }
		}
		catch (Exception exception)
		{
			Utils.sendFailure(ctx.getSource(), "mocap.error.unable_to_get_argument");
			return 0;
		}

		return 1;
	}

	private static int scenesAddTo(CommandContext<CommandSource> ctx)
	{
		try
		{
			String name = StringArgumentType.getString(ctx, "sceneName");

			String toAdd = StringArgumentType.getString(ctx, "toAdd");
			SceneInfo.Subscene subscene = new SceneInfo.Subscene(toAdd);

			try
			{
				subscene.startDelay = DoubleArgumentType.getDouble(ctx, "startDelay");
				subscene.startPos[0] = DoubleArgumentType.getDouble(ctx, "offsetX");
				subscene.startPos[1] = DoubleArgumentType.getDouble(ctx, "offsetY");
				subscene.startPos[2] = DoubleArgumentType.getDouble(ctx, "offsetZ");
				subscene.playerName = StringArgumentType.getString(ctx, "playerName");

				if (subscene.playerName.length() > 16)
				{
					Utils.sendFailure(ctx.getSource(), "mocap.scenes.add_to.error");
					Utils.sendFailure(ctx.getSource(), "mocap.scenes.add_to.error.too_long_name");
					return 0;
				}

				if (subscene.playerName.contains(" "))
				{
					Utils.sendFailure(ctx.getSource(), "mocap.scenes.add_to.error");
					Utils.sendFailure(ctx.getSource(), "mocap.scenes.add_to.error.contain_spaces");
					return 0;
				}

				subscene.mineskinURL = StringArgumentType.getString(ctx, "mineskinURL");
			}
			catch (Exception ignore) {}

			if (!SceneFile.addElement(ctx.getSource(), name, subscene.sceneToStr())) { return 0; }
		}
		catch (Exception exception)
		{
			Utils.sendFailure(ctx.getSource(), "mocap.error.unable_to_get_argument");
			return 0;
		}

		return 1;
	}

	private static int scenesRemoveFrom(CommandContext<CommandSource> ctx)
	{
		try
		{
			String name = StringArgumentType.getString(ctx, "sceneName");
			int pos = IntegerArgumentType.getInteger(ctx, "toRemove");

			if (!SceneFile.removeElement(ctx.getSource(), name, pos)) { return 0; }
		}
		catch (Exception exception)
		{
			Utils.sendFailure(ctx.getSource(), "mocap.error.unable_to_get_argument");
			return 0;
		}

		return 1;
	}

	private static int scenesListElements(CommandContext<CommandSource> ctx)
	{
		try
		{
			String name = StringArgumentType.getString(ctx, "name");
			return Scenes.listElements(ctx.getSource(), name);
		}
		catch (Exception exception)
		{
			Utils.sendFailure(ctx.getSource(), "mocap.error.unable_to_get_argument");
			return 0;
		}
	}

	private static int playingStart(CommandContext<CommandSource> ctx)
	{
		String name, playerName = null, mineskinURL = null;

		try
		{
			name = StringArgumentType.getString(ctx, "name");
		}
		catch (Exception exception)
		{
			Utils.sendFailure(ctx.getSource(), "mocap.error.unable_to_get_argument");
			return 0;
		}

		try
		{
			playerName = StringArgumentType.getString(ctx, "playerName");
			mineskinURL = StringArgumentType.getString(ctx, "mineskinURL");
		}
		catch (Exception ignore) {}

		try
		{
			return Playing.start(ctx.getSource(), name, playerName, mineskinURL);
		}
		catch (Exception exception)
		{
			Utils.sendFailure(ctx.getSource(), "mocap.playing.start.error");
			return 0;
		}
	}

	private static int playingStop(CommandContext<CommandSource> ctx)
	{
		try
		{
			int id = IntegerArgumentType.getInteger(ctx, "id");
			Playing.stop(ctx.getSource(), id);
		}
		catch (Exception exception)
		{
			Utils.sendFailure(ctx.getSource(), "mocap.error.unable_to_get_argument");
			return 0;
		}

		return 1;
	}

	private static int info(CommandContext<CommandSource> ctx)
	{
		Utils.sendSuccessLiteral(ctx.getSource(), MocapMod.getFullName());
		Utils.sendSuccessLiteral(ctx.getSource(), "Author: mt1006 (mt1006x)");
		return 1;
	}

	private static int help(CommandContext<CommandSource> ctx)
	{
		Utils.sendSuccess(ctx.getSource(), "mocap.help", MocapMod.getName());
		return 1;
	}
}
