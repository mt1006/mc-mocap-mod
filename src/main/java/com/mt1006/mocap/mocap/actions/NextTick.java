package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.vector.Vector3i;

public class NextTick implements Action
{
	public void write(RecordingFile.Writer writer)
	{
		writer.addByte(NEXT_TICK);
	}

	@Override public int execute(PlayerList packetTargets, FakePlayer fakePlayer, Vector3i blockOffset)
	{
		return RET_NEXT_TICK;
	}
}
