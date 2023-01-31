package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.mocap.playing.PlayerActions;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class HeadRotation implements Action
{
	private final float headRotY;

	public HeadRotation(Player player)
	{
		headRotY = player.yHeadRot;
	}

	public HeadRotation(RecordingFile.Reader reader)
	{
		headRotY = reader.readFloat();
	}

	public void write(RecordingFile.Writer writer, @Nullable PlayerActions actions)
	{
		if (!differs(actions)) { return; }
		writer.addByte(HEAD_ROTATION);
		writer.addFloat(headRotY);
	}

	public boolean differs(@Nullable PlayerActions actions)
	{
		if (actions == null) { return true; }
		return headRotY != actions.headRotation.headRotY;
	}

	@Override public int execute(PlayerList packetTargets, FakePlayer fakePlayer, Vec3i blockOffset)
	{
		packetTargets.broadcastAll(new ClientboundRotateHeadPacket(fakePlayer, (byte)Math.floor(headRotY * 256.0f / 360.0f)));
		return RET_OK;
	}
}
