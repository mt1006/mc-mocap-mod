package com.mt1006.mocap.mocap.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.mocap.playing.PlayerActions;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
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

	public static RecordingFile.Writer recording = new RecordingFile.Writer();
	public static State state = State.NOT_RECORDING;
	public static ServerPlayer serverPlayer = null;
	public static PlayerActions previousPlayerState = null;

	public static int start(CommandSourceStack commandSource, ServerPlayer serverPlayer)
	{
		switch (state)
		{
			case NOT_RECORDING:
				Recording.serverPlayer = serverPlayer;
				Utils.sendSuccess(commandSource, "mocap.commands.recording.start.waiting_for_action");
				state = State.WAITING_FOR_ACTION;
				break;

			case WAITING_FOR_ACTION:
				if (serverPlayer != Recording.serverPlayer)
				{
					Utils.sendFailure(commandSource, "mocap.commands.recording.start.different_player");
					Utils.sendFailure(commandSource, "mocap.commands.recording.start.different_player.tip");
					return 0;
				}

				previousPlayerState = null;
				state = State.RECORDING;
				Utils.sendSuccess(commandSource, "mocap.commands.recording.start.recording_started");
				break;

			case RECORDING:
				Utils.sendFailure(commandSource, "mocap.commands.recording.start.already_recording");
				Utils.sendFailure(commandSource, "mocap.commands.recording.start.already_recording.tip");
				return 0;

			case WAITING_FOR_DECISION:
				Utils.sendFailure(commandSource, "mocap.commands.recording.stop.waiting_for_decision");
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
				Utils.sendFailure(commandSource, "mocap.commands.recording.stop.server_not_recording");
				return 0;

			case WAITING_FOR_ACTION:
				Utils.sendSuccess(commandSource, "mocap.commands.recording.stop.stop_waiting_for_action");
				state = State.NOT_RECORDING;
				break;

			case RECORDING:
				Utils.sendSuccess(commandSource, "mocap.commands.recording.stop.waiting_for_decision");
				state = State.WAITING_FOR_DECISION;
				break;

			case WAITING_FOR_DECISION:
				recording.clear();
				Utils.sendSuccess(commandSource, "mocap.commands.recording.stop.recording_discarded");
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
				Utils.sendFailure(commandSource, "mocap.commands.recording.save.nothing_to_save");
				Utils.sendFailure(commandSource, "mocap.commands.recording.save.nothing_to_save.tip");
				return 0;

			case WAITING_FOR_ACTION:
				Utils.sendFailure(commandSource, "mocap.commands.recording.save.waiting_for_action");
				Utils.sendFailure(commandSource, "mocap.commands.recording.save.waiting_for_action.tip");
				return 0;

			case RECORDING:
				Utils.sendFailure(commandSource, "mocap.commands.recording.save.recording_not_stopped");
				Utils.sendFailure(commandSource, "mocap.commands.recording.save.recording_not_stopped.tip");
				return 0;

			case WAITING_FOR_DECISION:
				if (RecordingFile.save(commandSource, name, recording)) { state = State.NOT_RECORDING; }
				else { return 0; }
				break;
		}

		return 1;
	}

	public static int list(CommandContext<CommandSourceStack> ctx)
	{
		CommandSourceStack commandSource = ctx.getSource();

		StringBuilder recordingsListStr = new StringBuilder();
		ArrayList<String> recordingsList = RecordingFile.list(commandSource);

		if (recordingsList == null)
		{
			recordingsListStr.append(" ").append(Utils.stringFromComponent("mocap.commands.playing.list.error"));
		}
		else if (!recordingsList.isEmpty())
		{
			for (String name : recordingsList)
			{
				recordingsListStr.append(" ").append(name);
			}
		}
		else
		{
			recordingsListStr.append(" ").append(Utils.stringFromComponent("mocap.commands.playing.list.empty"));
		}

		Utils.sendSuccess(commandSource, "mocap.commands.playing.list.recordings", new String(recordingsListStr));
		return 1;
	}

	public static int state(CommandContext<CommandSourceStack> ctx)
	{
		CommandSourceStack commandSource = ctx.getSource();

		switch (state)
		{
			case NOT_RECORDING:
				Utils.sendSuccess(commandSource, "mocap.commands.recording.state.not_recording");
				break;

			case WAITING_FOR_ACTION:
				Utils.sendSuccess(commandSource, "mocap.commands.recording.state.waiting_for_action");
				Utils.sendSuccess(commandSource, "mocap.commands.recording.state.player_name");
				break;

			case RECORDING:
				Utils.sendSuccess(commandSource, "mocap.commands.recording.state.recording");
				Utils.sendSuccess(commandSource, "mocap.commands.recording.state.player_name", serverPlayer.getName());
				break;

			case WAITING_FOR_DECISION:
				Utils.sendSuccess(commandSource, "mocap.commands.recording.state.waiting_for_decision");
				break;
		}
		return 1;
	}
}