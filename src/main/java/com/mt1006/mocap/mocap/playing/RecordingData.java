package com.mt1006.mocap.mocap.playing;

import com.mt1006.mocap.mocap.actions.Action;
import com.mt1006.mocap.mocap.actions.BlockAction;
import com.mt1006.mocap.mocap.commands.Settings;
import com.mt1006.mocap.mocap.files.Files;
import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.utils.FakePlayer;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Vec3i;
import net.minecraft.server.players.PlayerList;

import java.util.ArrayList;
import java.util.List;

public class RecordingData
{
	public final List<Action> actions = new ArrayList<>();
	public final List<BlockAction> blockActions = new ArrayList<>();
	public final double[] startPos = new double[3];
	public final float[] startRot = new float[2];

	public boolean load(CommandSourceStack commandSource, String name)
	{
		byte[] data = Files.loadFile(Files.getRecordingFile(commandSource, name));
		if (data == null) { return false; }
		return load(commandSource, new RecordingFile.Reader(data));
	}

	public boolean load(CommandSourceStack commandSource, RecordingFile.Reader reader)
	{
		byte version = reader.readByte();
		if (version > RecordingFile.RECORDING_VERSION)
		{
			Utils.sendFailure(commandSource, "mocap.commands.playing.start.error");
			Utils.sendFailure(commandSource, "mocap.commands.playing.start.error.load_header");
			return false;
		}

		startPos[0] = reader.readDouble();
		startPos[1] = reader.readDouble();
		startPos[2] = reader.readDouble();

		startRot[0] = reader.readFloat();
		startRot[1] = reader.readFloat();

		while (reader.canRead())
		{
			Action action = Action.readAction(reader);
			if (action != null)
			{
				actions.add(action);
				if (action instanceof BlockAction) { blockActions.add((BlockAction)action); }
			}
			else
			{
				return false;
			}
		}
		return true;
	}

	public void preExecute(FakePlayer fakePlayer, Vec3i blockOffset)
	{
		if (Settings.SET_BLOCK_STATES.val)
		{
			for (int i = blockActions.size() - 1; i >= 0; i--)
			{
				blockActions.get(i).preExecute(fakePlayer, blockOffset);
			}
		}
	}

	public int executeNext(PlayerList packetTargets, FakePlayer fakePlayer, Vec3i blockOffset, int pos)
	{
		if (pos >= actions.size()) { return Action.RET_END; }

		try
		{
			Action nextAction = actions.get(pos);
			if (!Settings.PLAY_BLOCK_ACTIONS.val && nextAction instanceof BlockAction) { return Action.RET_OK; }

			return nextAction.execute(packetTargets, fakePlayer, blockOffset);
		}
		catch (Exception exception)
		{
			return Action.RET_ERROR;
		}
	}
}
