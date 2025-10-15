package net.mt1006.mocap.api.v1.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.files.SceneFiles;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MocapTransformations
{
	/**
	 * @return rotation in degrees
	 */
	double getRotation();

	/**
	 * @param rot rotation in degrees
	 */
	MocapTransformations withRotation(double rot);

	MocapMirror getMirror();

	MocapTransformations withMirror(MocapMirror mirror);

	/**
	 * Gets scale of player or other main playback entity in case when player_as_entity modifier is active.
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
	MocapTransformations withScaleOfPlayer(double scale);

	/**
	 * Gets scale of entire scene.
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
	MocapTransformations withScaleOfScene(double scale);

	MocapOffset getOffset();

	default MocapTransformations withOffset(Vec3 offset)
	{
		return withOffset(MocapOffset.fromVec3(offset));
	}

	MocapTransformations withOffset(MocapOffset offset);

	MocapTransformationsConfig getConfig();

	MocapTransformations withConfig(MocapTransformationsConfig config);

	Vec3 calculateCenter(Vec3 startPos);

	Vec3 apply(Vec3 point, Vec3 center);

	List<? extends BlockPos> applyToBlockPos(List<? extends BlockPos> inputList, Vec3 center);

	BlockState applyToBlockState(BlockState blockState);

	double applyToRotation(double rot);

	void applyScaleToPlayer(Entity entity);

	void applyScaleToEntity(Entity entity);

	@ApiStatus.Internal
	boolean areDefault();

	@ApiStatus.Internal
	MocapTransformations mergeWithParent(MocapTransformations parent);

	@ApiStatus.Internal
	@Nullable SceneFiles.Writer save();

	@ApiStatus.Internal
	void list(CommandOutput out);

	@ApiStatus.Internal
	@Nullable MocapTransformations modify(FullCommandInfo info, int propertyNodePos);
}
