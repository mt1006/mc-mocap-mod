package net.mt1006.mocap.api.v1.extension;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public interface MocapPositionTransformer
{
	Vec3 transformPos(Vec3 point);

	List<? extends BlockPos> transformBlockPos(BlockPos blockPos);

	BlockState transformBlockState(BlockState blockState);

	float transformRotation(float rot);
}
