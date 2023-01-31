package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.core.Vec3i;
import net.minecraft.server.players.PlayerList;

public class NextTick implements Action
{
	public void write(RecordingFile.Writer writer)
	{
		writer.addByte(NEXT_TICK);
	}

	@Override public int execute(PlayerList packetTargets, FakePlayer fakePlayer, Vec3i blockOffset)
	{
		return RET_NEXT_TICK;
	}
}
