package com.mt1006.mocap.mocap.playing;

import com.mt1006.mocap.mocap.actions.Action;
import com.mt1006.mocap.mocap.actions.BlockAction;
import com.mt1006.mocap.mocap.actions.NextTick;
import com.mt1006.mocap.mocap.files.Files;
import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.settings.Settings;
import com.mt1006.mocap.utils.EntityData;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;

public class RecordingData
{
	public final List<Action> actions = new ArrayList<>();
	public final List<BlockAction> blockActions = new ArrayList<>();
	public final double[] startPos = new double[3];
	public final float[] startRot = new float[2];
	public boolean endsWithDeath = false;
	public byte version = 0;
	public long fileSize = 0;
	public long tickCount = 0;

	public boolean load(CommandSource commandSource, String name)
	{
		byte[] data = Files.loadFile(Files.getRecordingFile(commandSource, name));
		if (data == null) { return false; }
		return load(commandSource, new RecordingFiles.FileReader(data));
	}

	public boolean load(CommandSource commandSource, RecordingFiles.FileReader reader)
	{
		fileSize = reader.getSize();
		version = reader.readByte();

		if (version > RecordingFiles.RECORDING_VERSION)
		{
			Utils.sendFailure(commandSource, "mocap.playing.start.error");
			Utils.sendFailure(commandSource, "mocap.playing.start.error.load_header");
			return false;
		}

		startPos[0] = reader.readDouble();
		startPos[1] = reader.readDouble();
		startPos[2] = reader.readDouble();

		startRot[0] = reader.readFloat();
		startRot[1] = reader.readFloat();

		if (version > 2)
		{
			endsWithDeath = reader.readBoolean();
		}

		while (reader.canRead())
		{
			Action action = Action.readAction(reader);
			if (action != null)
			{
				actions.add(action);
				if (action instanceof BlockAction) { blockActions.add((BlockAction)action); }
				if (action instanceof NextTick) { tickCount++; }
			}
			else
			{
				return false;
			}
		}
		return true;
	}

	public void preExecute(Entity entity, Vector3i blockOffset)
	{
		if (Settings.SET_BLOCK_STATES.val)
		{
			for (int i = blockActions.size() - 1; i >= 0; i--)
			{
				blockActions.get(i).preExecute(entity, blockOffset);
			}
		}
	}

	public Action.Result executeNext(PlayingContext ctx, int pos)
	{
		if (pos >= actions.size()) { return Action.Result.END; }
		if (pos == 0) { firstExecute(ctx.packetTargets, ctx.entity); }

		try
		{
			Action nextAction = actions.get(pos);
			if (!Settings.PLAY_BLOCK_ACTIONS.val && nextAction instanceof BlockAction) { return Action.Result.OK; }

			return nextAction.execute(ctx);
		}
		catch (Exception exception)
		{
			return Action.Result.ERROR;
		}
	}

	private void firstExecute(PlayerList packetTargets, Entity entity)
	{
		if (entity instanceof PlayerEntity)
		{
			new EntityData(entity, EntityData.PLAYER_SKIN_PARTS, (byte)0b01111111).broadcast(packetTargets);
		}
	}
}
