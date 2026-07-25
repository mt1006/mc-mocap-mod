package net.mt1006.mocap.api.v1.extension.actions;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.extension.MocapPositionTransformer;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;

public interface MocapBasicActionContext
{
	MocapRecordingData getRecordingData();

	/**
	 * Get entity on which action should be executed.
	 * This isn't constant, and may change e.g. if action is wrapped as ENTITY_ACTION.
	 * If you want to get main entity, use getMainEntity().
	 * @return entity on which action should be executed
	 * @see MocapBasicActionContext#getMainEntity()
	 */
	Entity getEntity();

	/**
	 * Returns main entity of a playback. That is player or another entity which
	 * is playing back recorded player movements (if "player as entity" modifier is used).
	 * If you want to get entity on which action should actually be executed, see getEntity().
	 * If you want to get main entity as ServerPlayer or LivingEntity see getRealOrDummyPlayer()
	 * or getLivingEntityOrDummyPlayer().
	 * @return main entity
	 * @see MocapBasicActionContext#getEntity()
	 * @see MocapActionContext#getRealOrDummyPlayer()
	 * @see MocapActionContext#getLivingEntityOrDummyPlayer()
	 */
	Entity getMainEntity();

	/**
	 * @return level (world) of a playback
	 */
	ServerLevel getLevel();

	/**
	 * @return configuration used by this playback
	 */
	MocapPlaybackConfig getConfig();

	/**
	 * @return modifiers of this playback
	 */
	MocapModifiers getModifiers();

	/**
	 * @return position transformer used by this playback
	 */
	MocapPositionTransformer getTransformer();
}
