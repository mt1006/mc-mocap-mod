package com.mt1006.mocap.mocap.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mt1006.mocap.mocap.playing.PlayerState;
import com.mt1006.mocap.utils.FileUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;

public class Recording
{
	public enum State
	{
		NOT_RECORDING,
		WAITING_FOR_ACTION,
		RECORDING,
		WAITING_FOR_DECISION
	}

	public static ArrayList<Byte> recording = new ArrayList<>();
	public static State state = State.NOT_RECORDING;
	public static ServerPlayer serverPlayer = null;
	public static PlayerState previousPlayerState = null;

	public static int start(CommandSourceStack commandSource, ServerPlayer serverPlayer)
	{
		switch (state)
		{
			case NOT_RECORDING:
				Recording.serverPlayer = serverPlayer;
				commandSource.sendSuccess(Component.translatable("mocap.commands.recording.start.waiting_for_action"), false);
				state = State.WAITING_FOR_ACTION;
				break;

			case WAITING_FOR_ACTION:
				if (serverPlayer != Recording.serverPlayer)
				{
					commandSource.sendFailure(Component.translatable("mocap.commands.recording.start.different_player"));
					commandSource.sendFailure(Component.translatable("mocap.commands.recording.start.different_player.tip"));
					return 0;
				}

				previousPlayerState = null;
				state = State.RECORDING;
				commandSource.sendSuccess(Component.translatable("mocap.commands.recording.start.recording_started"), false);
				break;

			case RECORDING:
				commandSource.sendFailure(Component.translatable("mocap.commands.recording.start.already_recording"));
				commandSource.sendFailure(Component.translatable("mocap.commands.recording.start.already_recording.tip"));
				return 0;

			case WAITING_FOR_DECISION:
				commandSource.sendFailure(Component.translatable("mocap.commands.recording.stop.waiting_for_decision"));
				return 0;
		}

		return 1;
	}

	public static int stop(CommandContext<CommandSourceStack> ctx)
	{
		CommandSourceStack commandSource = ctx.getSource();

		if (Settings.RECORDING_SYNC.val) { Playing.stopAll(ctx); }

		switch (state)
		{
			case NOT_RECORDING:
				commandSource.sendFailure(Component.translatable("mocap.commands.recording.stop.server_not_recording"));
				return 0;

			case WAITING_FOR_ACTION:
				commandSource.sendSuccess(Component.translatable("mocap.commands.recording.stop.stop_waiting_for_action"), false);
				state = State.NOT_RECORDING;
				break;

			case RECORDING:
				commandSource.sendSuccess(Component.translatable("mocap.commands.recording.stop.waiting_for_decision"), false);
				state = State.WAITING_FOR_DECISION;
				break;

			case WAITING_FOR_DECISION:
				recording.clear();
				commandSource.sendSuccess(Component.translatable("mocap.commands.recording.stop.recording_discarded"), false);
				state = State.NOT_RECORDING;
				break;
		}

		return 1;
	}

	public static int save(CommandSourceStack commandSource, String name)
	{
		switch (state)
		{
			case NOT_RECORDING:
				commandSource.sendFailure(Component.translatable("mocap.commands.recording.save.nothing_to_save"));
				commandSource.sendFailure(Component.translatable("mocap.commands.recording.save.nothing_to_save.tip"));
				return 0;

			case WAITING_FOR_ACTION:
				commandSource.sendFailure(Component.translatable("mocap.commands.recording.save.waiting_for_action"));
				commandSource.sendFailure(Component.translatable("mocap.commands.recording.save.waiting_for_action.tip"));
				return 0;

			case RECORDING:
				commandSource.sendFailure(Component.translatable("mocap.commands.recording.save.recording_not_stopped"));
				commandSource.sendFailure(Component.translatable("mocap.commands.recording.save.recording_not_stopped.tip"));
				return 0;

			case WAITING_FOR_DECISION:
				if (FileUtils.saveRecording(commandSource, name, recording)) { state = State.NOT_RECORDING; }
				else { return 0; }
				break;
		}

		return 1;
	}

	public static int list(CommandContext<CommandSourceStack> ctx)
	{
		CommandSourceStack commandSource = ctx.getSource();

		StringBuilder recordingsListStr = new StringBuilder();
		ArrayList<String> recordingsList = FileUtils.recordingsList(commandSource);

		if (recordingsList == null)
		{
			recordingsListStr.append(" ");
			recordingsListStr.append(Component.translatable("mocap.commands.playing.list.error").getString());
		}
		else if (!recordingsList.isEmpty())
		{
			for (String name : recordingsList)
			{
				recordingsListStr.append(" ");
				recordingsListStr.append(name);
			}
		}
		else
		{
			recordingsListStr.append(" ");
			recordingsListStr.append(Component.translatable("mocap.commands.playing.list.empty").getString());
		}

		commandSource.sendSuccess(Component.translatable("mocap.commands.playing.list.recordings",
				new String(recordingsListStr)), false);
		return 1;
	}

	public static int state(CommandContext<CommandSourceStack> ctx)
	{
		CommandSourceStack commandSource = ctx.getSource();

		switch (state)
		{
			case NOT_RECORDING:
				commandSource.sendSuccess(Component.translatable("mocap.commands.recording.state.not_recording"), false);
				break;

			case WAITING_FOR_ACTION:
				commandSource.sendSuccess(Component.translatable("mocap.commands.recording.state.waiting_for_action"), false);
				commandSource.sendSuccess(Component.translatable("mocap.commands.recording.state.player_name", serverPlayer.getName()), false);
				break;

			case RECORDING:
				commandSource.sendSuccess(Component.translatable("mocap.commands.recording.state.recording"), false);
				commandSource.sendSuccess(Component.translatable("mocap.commands.recording.state.player_name", serverPlayer.getName()), false);
				break;

			case WAITING_FOR_DECISION:
				commandSource.sendSuccess(Component.translatable("mocap.commands.recording.state.waiting_for_decision"), false);
				break;
		}
		return 1;
	}
}
