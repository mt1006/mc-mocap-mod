package com.mt1006.mocap.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mt1006.mocap.MocapMod;
import com.mt1006.mocap.mocap.SceneInfo;
import com.mt1006.mocap.mocap.Playing;
import com.mt1006.mocap.mocap.Recording;
import com.mt1006.mocap.mocap.Scenes;
import com.mt1006.mocap.utils.FileUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Collection;

public class MocapCommand
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
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
						then(Commands.argument("playerName", StringArgumentType.string()).executes(MocapCommand::scenesAddTo))))))))).
					then(Commands.literal("removeFrom").
						then(Commands.argument("sceneName", StringArgumentType.string()).
						then(Commands.argument("toRemove", IntegerArgumentType.integer()).executes(MocapCommand::scenesRemoveFrom)))).
					then(Commands.literal("listElements").
						then(Commands.argument("name", StringArgumentType.string()).executes(MocapCommand::scenesListElements))).
					then(Commands.literal("list").executes(Scenes::list))).
				then(Commands.literal("playing").
					then(Commands.literal("start").
						then(Commands.argument("name", StringArgumentType.string()).executes(MocapCommand::playingStart).
						then(Commands.argument("startPos", Vec3Argument.vec3()).executes(MocapCommand::playingStart)))).
					then(Commands.literal("stop").
						then(Commands.argument("id", IntegerArgumentType.integer()).executes(MocapCommand::playingStop))).
					then(Commands.literal("stopAll").executes(Playing::stopAll)).
					then(Commands.literal("list").executes(Playing::list))).
				then(Commands.literal("info").executes(MocapCommand::info)).
				then(Commands.literal("help").executes(MocapCommand::help)));
	}

	private static int recordingStart(CommandContext<CommandSourceStack> ctx)
	{
		ServerPlayer serverPlayer;

		try
		{
			Collection<GameProfile> gameProfiles = GameProfileArgument.getGameProfiles(ctx, "player");

			if (gameProfiles.size() != 1)
			{
				ctx.getSource().sendFailure(new TranslatableComponent("mocap.commands.recording.start.player_not_found"));
				return 0;
			}

			String nickname = gameProfiles.iterator().next().getName();
			serverPlayer = ctx.getSource().getServer().getPlayerList().getPlayerByName(nickname);

			if(serverPlayer == null)
			{
				ctx.getSource().sendFailure(new TranslatableComponent("mocap.commands.recording.start.player_not_found"));
				return 0;
			}
		}
		catch (Exception exception)
		{
			Entity entity = ctx.getSource().getEntity();

			if (!(entity instanceof ServerPlayer))
			{
				ctx.getSource().sendFailure(new TranslatableComponent("mocap.commands.recording.start.player_not_specified"));
				return 0;
			}

			serverPlayer = (ServerPlayer)entity;
		}

		return Recording.start(ctx.getSource(), serverPlayer);
	}

	private static int recordingSave(CommandContext<CommandSourceStack> ctx)
	{
		try
		{
			String name = StringArgumentType.getString(ctx, "name");
			return Recording.save(ctx.getSource(), name);
		}
		catch (Exception exception)
		{
			ctx.getSource().sendFailure(new TranslatableComponent("mocap.commands.error.unable_to_get_argument"));
			return 0;
		}
	}

	private static int recordingRemove(CommandContext<CommandSourceStack> ctx)
	{
		try
		{
			String name = StringArgumentType.getString(ctx, "name");
			if (!FileUtils.removeRecording(ctx.getSource(), name)) { return 0; }
		}
		catch (Exception exception)
		{
			ctx.getSource().sendFailure(new TranslatableComponent("mocap.commands.error.unable_to_get_argument"));
			return 0;
		}

		return 1;
	}

	private static int scenesAdd(CommandContext<CommandSourceStack> ctx)
	{
		try
		{
			String name = StringArgumentType.getString(ctx, "name");
			if (name.charAt(0) == '.') { name = name.substring(1); }
			if (!FileUtils.addScene(ctx.getSource(), name)) { return 0; }
		}
		catch (Exception exception)
		{
			ctx.getSource().sendFailure(new TranslatableComponent("mocap.commands.error.unable_to_get_argument"));
			return 0;
		}

		return 1;
	}

	private static int scenesRemove(CommandContext<CommandSourceStack> ctx)
	{
		try
		{
			String name = StringArgumentType.getString(ctx, "name");
			if (name.charAt(0) == '.') { name = name.substring(1); }
			if (!FileUtils.removeScene(ctx.getSource(), name)) { return 0; }
		}
		catch (Exception exception)
		{
			ctx.getSource().sendFailure(new TranslatableComponent("mocap.commands.error.unable_to_get_argument"));
			return 0;
		}

		return 1;
	}

	private static int scenesAddTo(CommandContext<CommandSourceStack> ctx)
	{
		try
		{
			String name = StringArgumentType.getString(ctx, "sceneName");
			if (name.charAt(0) == '.') { name = name.substring(1); }

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
					ctx.getSource().sendFailure(new TranslatableComponent("mocap.commands.scenes.add_to.too_long_name"));
					return 0;
				}
				if (subscene.playerName.contains(" "))
				{
					ctx.getSource().sendFailure(new TranslatableComponent("mocap.commands.scenes.add_to.contain_spaces"));
					return 0;
				}
			}
			catch (Exception ignore) {}

			if (!FileUtils.addToScene(ctx.getSource(), name, subscene.sceneToStr())) { return 0; }
		}
		catch (Exception exception)
		{
			ctx.getSource().sendFailure(new TranslatableComponent("mocap.commands.error.unable_to_get_argument"));
			return 0;
		}

		return 1;
	}

	private static int scenesRemoveFrom(CommandContext<CommandSourceStack> ctx)
	{
		try
		{
			String name = StringArgumentType.getString(ctx, "sceneName");
			if (name.charAt(0) == '.') { name = name.substring(1); }
			int pos = IntegerArgumentType.getInteger(ctx, "toRemove");

			if (!FileUtils.removeFromScene(ctx.getSource(), name, pos)) { return 0; }
		}
		catch (Exception exception)
		{
			ctx.getSource().sendFailure(new TranslatableComponent("mocap.commands.error.unable_to_get_argument"));
			return 0;
		}

		return 1;
	}

	private static int scenesListElements(CommandContext<CommandSourceStack> ctx)
	{
		try
		{
			String name = StringArgumentType.getString(ctx, "name");
			if (name.charAt(0) == '.') { name = name.substring(1); }
			return Scenes.listElements(ctx.getSource(), name);
		}
		catch (Exception exception)
		{
			ctx.getSource().sendFailure(new TranslatableComponent("mocap.commands.error.unable_to_get_argument"));
			return 0;
		}
	}

	private static int playingStart(CommandContext<CommandSourceStack> ctx)
	{
		String name;
		try
		{
			name = StringArgumentType.getString(ctx, "name");
		}
		catch (Exception exception)
		{
			ctx.getSource().sendFailure(new TranslatableComponent("mocap.commands.error.unable_to_get_argument"));
			return 0;
		}

		try
		{
			return Playing.start(ctx.getSource(), name);
		}
		catch (Exception exception)
		{
			ctx.getSource().sendFailure(new TranslatableComponent("mocap.commands.playing.start.failed"));
			return 0;
		}
	}

	private static int playingStop(CommandContext<CommandSourceStack> ctx)
	{
		try
		{
			int id = IntegerArgumentType.getInteger(ctx, "id");
			Playing.stop(ctx.getSource(), id);
		}
		catch (Exception exception)
		{
			ctx.getSource().sendFailure(new TranslatableComponent("mocap.commands.error.unable_to_get_argument"));
			return 0;
		}

		return 1;
	}

	private static int info(CommandContext<CommandSourceStack> ctx)
	{
		ctx.getSource().sendSuccess(new TextComponent(MocapMod.getFullName()), false);
		ctx.getSource().sendSuccess(new TextComponent("Author: mt1006 (mt1006x)"), false);
		return 1;
	}

	private static int help(CommandContext<CommandSourceStack> ctx)
	{
		ctx.getSource().sendSuccess(new TranslatableComponent("mocap.commands.help", MocapMod.getName()), false);
		return 1;
	}
}
