package com.mt1006.mocap.events;

import com.mt1006.mocap.mocap.commands.Playing;
import com.mt1006.mocap.mocap.commands.Recording;
import com.mt1006.mocap.mocap.playing.PlayerActions;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.server.MinecraftServer;

public class ServerTickEvent
{
	private static PlayerActions previousActions = null;

	public static void onEndTick(MinecraftServer server)
	{
		if (Recording.state == Recording.State.WAITING_FOR_ACTION)
		{
			PlayerActions playerActions = new PlayerActions(Recording.serverPlayer);
			if (playerActions.differs(previousActions))
			{
				Recording.previousPlayerState = null;
				Recording.state = Recording.State.RECORDING;
				Utils.sendSystemMessage(Recording.serverPlayer, "mocap.recording.start.recording_started");
				previousActions = null;
			}
			else
			{
				previousActions = playerActions;
			}
		}

		if (Recording.state == Recording.State.RECORDING)
		{
			PlayerActions playerState = new PlayerActions(Recording.serverPlayer);
			playerState.saveDifference(Recording.recording, Recording.previousPlayerState);
			Recording.previousPlayerState = playerState;
		}

		Playing.onTick();
	}
}
