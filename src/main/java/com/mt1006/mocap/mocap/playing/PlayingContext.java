package com.mt1006.mocap.mocap.playing;

import com.mt1006.mocap.events.PlayerConnectionEvent;
import com.mt1006.mocap.mocap.settings.Settings;
import com.mt1006.mocap.network.MocapPacketS2C;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class PlayingContext
{
	public final PlayerList packetTargets;
	public final Entity mainEntity;
	public final Level level;
	public final Vec3 offset;
	public final Vec3i blockOffset;
	public final Map<Integer, Entity> entityMap = new HashMap<>();
	public Entity entity;
	public boolean entityRemoved = false;
	private Vec3 position;

	public PlayingContext(PlayerList packetTargets, Entity entity, Vec3 offset, Vec3i blockOffset)
	{
		this.packetTargets = packetTargets;
		this.mainEntity = entity;
		this.level = entity.level;
		this.offset = offset;
		this.blockOffset = blockOffset;
		this.entity = entity;
		this.position = entity.position();
	}

	public void broadcast(Packet<?> packet)
	{
		packetTargets.broadcastAll(packet);
	}

	public void removeEntities()
	{
		if (!entityRemoved)
		{
			if (entity instanceof FakePlayer)
			{
				FakePlayer fakePlayer = (FakePlayer)entity;
				if (PlayerConnectionEvent.nocolPlayers.contains(fakePlayer.getUUID()))
				{
					for (ServerPlayer player : PlayerConnectionEvent.players)
					{
						MocapPacketS2C.sendNocolPlayerRemove(player, fakePlayer.getUUID());
						PlayerConnectionEvent.removeNocolPlayer(fakePlayer.getUUID());
					}
				}
				broadcast(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, fakePlayer));
				fakePlayer.remove(Entity.RemovalReason.KILLED);
			}
			else
			{
				removeEntity(entity);
			}
		}

		entityMap.values().forEach(PlayingContext::removeEntity);
		entityMap.clear();
	}

	public void shiftPosition(double x, double y, double z, float rotY, float rotX)
	{
		position = position.add(x, y, z);
		entity.moveTo(position.x, position.y, position.z, rotY, rotX);
	}

	private static void removeEntity(Entity entity)
	{
		switch (Settings.ENTITIES_AFTER_PLAYBACK.val)
		{
			case -1:
				entity.setNoGravity(false);
				entity.setInvulnerable(false);
				entity.removeTag(Playing.MOCAP_ENTITY_TAG);
				if (entity instanceof Mob) { ((Mob)entity).setNoAi(false); }
				break;

			case 0:
				break;

			case 2:
				entity.invulnerableTime = 0; // for sound effect
				entity.kill();
				break;

			default:
				entity.remove(Entity.RemovalReason.KILLED);
				break;
		}
	}
}
