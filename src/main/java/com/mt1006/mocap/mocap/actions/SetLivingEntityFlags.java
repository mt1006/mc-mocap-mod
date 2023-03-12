package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.mocap.playing.PlayerActions;
import com.mt1006.mocap.utils.EntityData;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.core.Vec3i;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class SetLivingEntityFlags implements Action
{
	private final byte livingEntityFlags;

	public SetLivingEntityFlags(Player player)
	{
		byte livingEntityFlags = 0;
		if (player.isUsingItem()) { livingEntityFlags |= 0x01; }
		if (player.getUsedItemHand() == InteractionHand.OFF_HAND) { livingEntityFlags |= 0x02; }
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

	@Override public int execute(PlayerList packetTargets, FakePlayer fakePlayer, Vec3i blockOffset)
	{
		new EntityData<>(fakePlayer, EntityData.LIVING_ENTITY_FLAGS, livingEntityFlags).broadcastAll(packetTargets);
		return RET_OK;
	}
}
