package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.mocap.playing.PlayerActions;
import com.mt1006.mocap.utils.EntityData;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3i;
import org.jetbrains.annotations.Nullable;

public class SetLivingEntityFlags implements Action
{
	private final byte livingEntityFlags;

	public SetLivingEntityFlags(PlayerEntity player)
	{
		byte livingEntityFlags = 0;
		if (player.isUsingItem()) { livingEntityFlags |= 0x01; }
		if (player.getUsedItemHand() == Hand.OFF_HAND) { livingEntityFlags |= 0x02; }
		if (player.isAutoSpinAttack()) { livingEntityFlags |= 0x04; }
		this.livingEntityFlags = livingEntityFlags;
	}

	public SetLivingEntityFlags(RecordingFile.Reader reader)
	{
		livingEntityFlags = reader.readByte();
	}

	public void write(RecordingFile.Writer writer, @Nullable PlayerActions actions)
	{
		if (!differs(actions)) { return; }
		writer.addByte(SET_LIVING_ENTITY_FLAGS);
		writer.addByte(livingEntityFlags);
	}

	public boolean differs(@Nullable PlayerActions actions)
	{
		if (actions == null) { return true; }
		return livingEntityFlags != actions.setLivingEntityFlags.livingEntityFlags;
	}

	@Override public int execute(PlayerList packetTargets, FakePlayer fakePlayer, Vector3i blockOffset)
	{
		packetTargets.broadcastAll(new EntityData<>(fakePlayer, EntityData.LIVING_ENTITY_FLAGS, livingEntityFlags).getPacket());
		return RET_OK;
	}
}
