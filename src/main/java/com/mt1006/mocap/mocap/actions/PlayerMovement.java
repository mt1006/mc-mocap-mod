package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.mocap.playing.PlayerActions;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class PlayerMovement implements Action
{
	private final double[] position = new double[3];
	private final float[] rotation = new float[2];
	private final boolean isOnGround;

	public PlayerMovement(Player player)
	{
		Vec3 playerPos = player.position();
		position[0] = playerPos.x;
		position[1] = playerPos.y;
		position[2] = playerPos.z;

		rotation[0] = player.getXRot();
		rotation[1] = player.getYRot();

		isOnGround = player.isOnGround();
	}

	public PlayerMovement(RecordingFile.Reader reader)
	{
		position[0] = reader.readDouble();
		position[1] = reader.readDouble();
		position[2] = reader.readDouble();

		rotation[0] = reader.readFloat();
		rotation[1] = reader.readFloat();

		isOnGround = reader.readBoolean();
	}

	public void writeAsHeader(RecordingFile.Writer writer)
	{
		writer.addDouble(position[0]);
		writer.addDouble(position[1]);
		writer.addDouble(position[2]);

		writer.addFloat(rotation[1]);
		writer.addFloat(rotation[0]);
	}

	public void write(RecordingFile.Writer writer, @Nullable PlayerActions actions)
	{
		if (!differs(actions)) { return; }
		writer.addByte(PLAYER_MOVEMENT);

		if (actions == null)
		{
			writer.addDouble(0.0);
			writer.addDouble(0.0);
			writer.addDouble(0.0);
		}
		else
		{
			writer.addDouble(position[0] - actions.playerMovement.position[0]);
			writer.addDouble(position[1] - actions.playerMovement.position[1]);
			writer.addDouble(position[2] - actions.playerMovement.position[2]);
		}

		writer.addFloat(rotation[0]);
		writer.addFloat(rotation[1]);

		writer.addBoolean(isOnGround);
	}

	public boolean differs(@Nullable PlayerActions actions)
	{
		if (actions == null) { return true; }
		return !Arrays.equals(position, actions.playerMovement.position) ||
				!Arrays.equals(rotation, actions.playerMovement.rotation) ||
				isOnGround != actions.playerMovement.isOnGround;
	}

	@Override public int execute(PlayerList packetTargets, FakePlayer fakePlayer, Vec3i blockOffset)
	{
		double relX = fakePlayer.getX() + position[0];
		double relY = fakePlayer.getY() + position[1];
		double relZ = fakePlayer.getZ() + position[2];
		fakePlayer.moveTo(relX, relY, relZ, rotation[1], rotation[0]);
		fakePlayer.setOnGround(isOnGround);
		fakePlayer.checkInsideBlocks();

		packetTargets.broadcastAll(new ClientboundTeleportEntityPacket(fakePlayer));
		return RET_OK;
	}
}
