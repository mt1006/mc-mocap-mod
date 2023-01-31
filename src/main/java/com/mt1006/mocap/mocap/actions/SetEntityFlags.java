package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.mocap.playing.PlayerActions;
import com.mt1006.mocap.utils.EntityData;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.core.Vec3i;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class SetEntityFlags implements Action
{
	private final byte entityFlags;

	public SetEntityFlags(Player player)
	{
		byte entityFlags = 0;
		if (player.isOnFire()) { entityFlags |= 0x01; }
		if (player.isShiftKeyDown()) { entityFlags |= 0x02; }
		if (player.isSprinting()) { entityFlags |= 0x08; }
		if (player.isSwimming()) { entityFlags |= 0x10; }
		if (player.isInvisible()) { entityFlags |= 0x20; }
		if (player.isGlowing()) { entityFlags |= 0x40; }
		if (player.isFallFlying()) { entityFlags |= 0x80; }
		this.entityFlags = entityFlags;
	}

	public SetEntityFlags(RecordingFile.Reader reader)
	{
		entityFlags = reader.readByte();
	}

	public void write(RecordingFile.Writer writer, @Nullable PlayerActions actions)
	{
		if (!differs(actions)) { return; }
		writer.addByte(SET_ENTITY_FLAGS);
		writer.addByte(entityFlags);
	}

	public boolean differs(@Nullable PlayerActions actions)
	{
		if (actions == null) { return true; }
		return entityFlags != actions.setEntityFlags.entityFlags;
	}

	@Override public int execute(PlayerList packetTargets, FakePlayer fakePlayer, Vec3i blockOffset)
	{
		packetTargets.broadcastAll(new EntityData<>(fakePlayer, EntityData.ENTITY_FLAGS, entityFlags).getPacket());
		return RET_OK;
	}
}
