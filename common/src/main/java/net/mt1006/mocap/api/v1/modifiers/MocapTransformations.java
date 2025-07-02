package net.mt1006.mocap.api.v1.modifiers;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface MocapTransformations
{
	/**
	 * @return rotation in degrees
	 */
	double getRotation();

	/**
	 * @param rot rotation in degrees
	 */
	MocapTransformations setRotation(double rot);

	MirrorVariant getMirror();

	MocapTransformations setMirror(MirrorVariant mirror);

	/**
	 * Sets scale of player or other main playback entity in case when player_as_entity modifier is active.
	 * Scale of player (or main playback entity in general) is only applied to this entity and doesn't affect its speed
	 * or position. It's similar to setting just the scale attribute. Final player scale (scale attribute value)
	 * is equal to player_scale * scene_scale.
	 * <p>
	 * <b>WARNING: Player scale modifier works only on Minecraft version 1.20.5 and later.</b>
	 */
	double getScaleOfPlayer();

	/**
	 * <b>WARNING: Player scale modifier works only on Minecraft version 1.20.5 and later.</b>
	 * @see MocapTransformations#getScaleOfPlayer()
	 */
	MocapTransformations setScaleOfPlayer(double scale);

	/**
	 * Sets scale of entire scene.
	 * Scale of scene is applied to all entities. It affects entity scale attribute and transforms all positions
	 * in the playback (so it affects speed). It's similar to scale transformation in geometric sense.
	 * Final player scale (scale attribute value) is equal to player_scale * scene_scale.
	 * <b>WARNING: Scene scale modifiers fully works only on Minecraft version 1.20.5 and later.
	 * On older versions it will transform positions, but won't change entity size.</b>
	 */
	double getScaleOfScene();

	/**
	 * <b>WARNING: Scene scale modifiers fully works only on Minecraft version 1.20.5 and later.
	 * On older versions it will transform positions, but won't change entity size.</b>
	 * @see MocapTransformations#getScaleOfScene()
	 */
	MocapTransformations setScaleOfScene(double scale);

	Vec3 getOffset();

	MocapTransformations setOffset(Vec3 offset);

	MocapTransformationsConfig getConfig();

	MocapTransformations setConfig(MocapTransformationsConfig config);

	void applyScaleToEntity(Entity entity);

	enum MirrorVariant
	{
		NONE, X, Z, XZ
	}
}
