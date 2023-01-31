package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.mocap.playing.PlayerActions;
import com.mt1006.mocap.utils.EntityData;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3i;
import org.jetbrains.annotations.Nullable;

public class SetMainHand implements Action
{
	private final byte mainHand;

	public SetMainHand(PlayerEntity player)
	{
		if (player.getMainArm() == HandSide.LEFT) { mainHand = 0; }
		else { mainHand = 1; }
	}

	public SetMainHand(RecordingFile.Reader reader)
	{
		mainHand = reader.readByte();
	}

	public void write(RecordingFile.Writer writer, @Nullable PlayerActions actions)
	{
		if (!differs(actions)) { return; }
		writer.addByte(SET_MAIN_HAND);
		writer.addByte(mainHand);
	}

	public boolean differs(@Nullable PlayerActions actions)
	{
		if (actions == null) { return true; }
		return mainHand != actions.setMainHand.mainHand;
	}

	@Override public int execute(PlayerList packetTargets, FakePlayer fakePlayer, Vector3i blockOffset)
	{
		packetTargets.broadcastAll(new EntityData<>(fakePlayer, EntityData.SET_MAIN_HAND, mainHand).getPacket());
		return RET_OK;
	}
}
