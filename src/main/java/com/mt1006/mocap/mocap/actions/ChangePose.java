package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.mocap.playing.PlayerActions;
import com.mt1006.mocap.utils.EntityData;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.core.Vec3i;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class ChangePose implements Action
{
	private final Pose pose;

	public ChangePose(Player player)
	{
		pose = player.getPose();
	}

	public ChangePose(RecordingFile.Reader reader)
	{
		pose = switch (reader.readInt())
		{
			default -> Pose.STANDING;
			case 2 -> Pose.FALL_FLYING;
			case 3 -> Pose.SLEEPING;
			case 4 -> Pose.SWIMMING;
			case 5 -> Pose.SPIN_ATTACK;
			case 6 -> Pose.CROUCHING;
			case 7 -> Pose.DYING;
		};
	}

	public void write(RecordingFile.Writer writer, @Nullable PlayerActions actions)
	{
		if (!differs(actions)) { return; }
		writer.addByte(CHANGE_POSE);

		if (pose == Pose.STANDING) { writer.addInt(1); }
		else if (pose == Pose.FALL_FLYING) { writer.addInt(2); }
		else if (pose == Pose.SLEEPING) { writer.addInt(3); }
		else if (pose == Pose.SWIMMING) { writer.addInt(4); }
		else if (pose == Pose.SPIN_ATTACK) { writer.addInt(5); }
		else if (pose == Pose.CROUCHING) { writer.addInt(6); }
		else if (pose == Pose.DYING) { writer.addInt(7); }
		else { writer.addInt(0); }
	}

	public boolean differs(@Nullable PlayerActions actions)
	{
		if (actions == null) { return true; }
		return pose != actions.changePose.pose;
	}

	@Override public int execute(PlayerList packetTargets, FakePlayer fakePlayer, Vec3i blockOffset)
	{
		packetTargets.broadcastAll(new EntityData<>(fakePlayer, EntityData.POSE, pose).getPacket());
		return RET_OK;
	}
}
