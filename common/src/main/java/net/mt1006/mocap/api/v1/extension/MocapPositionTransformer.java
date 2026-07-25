package net.mt1006.mocap.api.v1.extension;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;

import java.util.List;

public interface MocapPositionTransformer
{
	/**
	 * Apply transformations to a point.
	 * @param point original point
	 * @return transformed point
	 */
	Vec3 transformPos(Vec3 point);

	/**
	 * Apply transformations to a block position.
	 * @param blockPos original block position
	 * @param allowScaled should multiple blocks in list be allowed
	 *                    (in most cases it should be {@code ctx.getConfig().getBlockAllowScaled()}).
	 * @return list of new block positions (it might contain multiple block positions, if transformations include scaling)
	 * @see MocapPlaybackConfig#getBlockAllowScaled()
	 */
	List<? extends BlockPos> transformBlockPos(BlockPos blockPos, boolean allowScaled);

	/**
	 * Apply transformations to a block state. This mainly includes rotating rotational blocks.
	 * @param blockState original block state
	 * @return transformed block state
	 */
	BlockState transformBlockState(BlockState blockState);

	/**
	 * Apply transformation to an absolute rotation.
	 * @param rot original absolute rotation
	 * @return transformed absolute rotation
	 */
	float transformRotation(float rot);
}
