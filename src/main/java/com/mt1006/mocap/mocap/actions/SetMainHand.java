package com.mt1006.mocap.mocap.actions;

import com.google.common.collect.ImmutableList;
import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.mocap.playing.PlayerActions;
import com.mt1006.mocap.utils.EntityData;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class SetMainHand implements Action
{
	private final byte mainHand;

	public SetMainHand(Player player)
	{
		if (player.getMainArm() == HumanoidArm.LEFT) { mainHand = 0; }
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

	@Override public int execute(PlayerList packetTargets, FakePlayer fakePlayer, Vec3i blockOffset)
	{
		packetTargets.broadcastAll(new EntityData<>(fakePlayer, EntityData.SET_MAIN_HAND, mainHand).getPacket());
		return RET_OK;
	}
}
