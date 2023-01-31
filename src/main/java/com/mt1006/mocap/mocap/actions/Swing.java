package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.mocap.playing.PlayerActions;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SAnimateHandPacket;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3i;
import org.jetbrains.annotations.Nullable;

public class Swing implements Action
{
	private final boolean swinging;
	private final int swingingTime;
	private final boolean offHand;

	public Swing(PlayerEntity player)
	{
		swinging = player.swinging;
		swingingTime = player.swingTime;
		offHand = player.swingingArm == Hand.OFF_HAND;
	}

	public Swing(RecordingFile.Reader reader)
	{
		swinging = true;
		swingingTime = 0;
		offHand = reader.readBoolean();
	}

	public void write(RecordingFile.Writer writer, @Nullable PlayerActions actions)
	{
		if (swinging && (actions == null || !actions.swing.swinging || actions.swing.swingingTime > swingingTime))
		{
			writer.addByte(SWING);
			writer.addBoolean(offHand);
		}
	}

	public boolean differs(@Nullable PlayerActions actions)
	{
		if (actions == null) { return true; }
		return swinging != actions.swing.swinging;
	}

	@Override public int execute(PlayerList packetTargets, FakePlayer fakePlayer, Vector3i blockOffset)
	{
		packetTargets.broadcastAll(new SAnimateHandPacket(fakePlayer, offHand ? 3 : 0));
		return RET_OK;
	}
}
