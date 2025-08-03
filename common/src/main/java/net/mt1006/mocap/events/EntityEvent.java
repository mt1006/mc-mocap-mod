package net.mt1006.mocap.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.mt1006.mocap.mocap.actions.EntityUpdate;
import net.mt1006.mocap.mocap.actions.Hurt;
import net.mt1006.mocap.mocap.playing.PlaybackManager;
import net.mt1006.mocap.mocap.recording.RecordingManager;

public class EntityEvent
{
	public static void onEntityHurt(LivingEntity entity)
	{
		if (RecordingManager.isActive() && entity.level() instanceof ServerLevel)
		{
			RecordingManager.byRecordedPlayer(entity).forEach((ctx) -> ctx.addAction(Hurt.INSTANCE));
			RecordingManager.listTrackedEntities(entity).forEach((e) -> e.getParent().addAction(EntityUpdate.hurt(e.getId())));
		}
	}

	public static boolean onEntityDrop(LivingEntity entity)
	{
		return !PlaybackManager.playbacks.isEmpty() && entity.getTags().contains(PlaybackManager.MOCAP_ENTITY_TAG);
	}

	public static void onPlayerRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer)
	{
		if (!RecordingManager.isActive() && !RecordingManager.waitingForRespawn.isEmpty()) { RecordingManager.waitingForRespawn.clear(); }
		if (RecordingManager.waitingForRespawn.isEmpty()) { return; }

		RecordingManager.waitingForRespawn.byKey.get(oldPlayer).forEach((ctx) -> ctx.onRespawn(newPlayer));
		RecordingManager.waitingForRespawn.byKey.removeAll(oldPlayer);
	}
}
