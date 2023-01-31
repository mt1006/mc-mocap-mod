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
		switch (reader.readInt())
		{
			default: pose = Pose.STANDING; break;
			case 2: pose = Pose.FALL_FLYING; break;
			case 3: pose = Pose.SLEEPING; break;
			case 4: pose = Pose.SWIMMING; break;
			case 5: pose = Pose.SPIN_ATTACK; break;
			case 6: pose = Pose.CROUCHING; break;
			case 7: pose = Pose.DYING; break;
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
