package com.mt1006.mocap.mocap.playing;

import com.mt1006.mocap.events.PlayerConnectionEvent;
import com.mt1006.mocap.mocap.settings.Settings;
import com.mt1006.mocap.network.MocapPacketS2C;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import java.util.HashMap;
import java.util.Map;

public class PlayingContext
{
	public final PlayerList packetTargets;
	public final Entity mainEntity;
	public final World level;
	public final Vector3d offset;
	public final Vector3i blockOffset;
	public final Map<Integer, Entity> entityMap = new HashMap<>();
	public Entity entity;
	public boolean entityRemoved = false;
	private Vector3d position;

	public PlayingContext(PlayerList packetTargets, Entity entity, Vector3d offset, Vector3i blockOffset)
	{
		this.packetTargets = packetTargets;
		this.mainEntity = entity;
		this.level = entity.level;
		this.offset = offset;
		this.blockOffset = blockOffset;
		this.entity = entity;
		this.position = entity.position();
	}

	public void broadcast(IPacket<?> packet)
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
					for (ServerPlayerEntity player : PlayerConnectionEvent.players)
					{
						MocapPacketS2C.sendNocolPlayerRemove(player, fakePlayer.getUUID());
						PlayerConnectionEvent.removeNocolPlayer(fakePlayer.getUUID());
					}
				}
				broadcast(new SPlayerListItemPacket(SPlayerListItemPacket.Action.REMOVE_PLAYER, fakePlayer));
				fakePlayer.remove();
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
				if (entity instanceof MobEntity) { ((MobEntity)entity).setNoAi(false); }
				break;

			case 0:
				break;

			case 2:
				entity.invulnerableTime = 0; // for sound effect
				entity.kill();
				break;

			default:
				entity.remove();
				break;
		}
	}
}
