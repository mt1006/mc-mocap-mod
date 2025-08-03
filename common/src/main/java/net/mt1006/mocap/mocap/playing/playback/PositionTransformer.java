package net.mt1006.mocap.mocap.playing.playback;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.extension.MocapPositionTransformer;
import net.mt1006.mocap.mocap.playing.modifiers.Transformations;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PositionTransformer implements MocapPositionTransformer
{
	private final Transformations transformations;
	private final @Nullable PositionTransformer parent;
	private final @Nullable Vec3 center;

	public PositionTransformer(Transformations transformations, @Nullable PositionTransformer parent, @Nullable Vec3 center)
	{
		this.transformations = transformations;
		this.parent = parent;
		this.center = center;
	}

	@Override public Vec3 transformPos(Vec3 point)
	{
		return transformPos(point, null);
	}

	private Vec3 transformPos(Vec3 point, @Nullable Vec3 childCenter)
	{
		if (childCenter == null && center == null) { throw new RuntimeException("Both childCenter and center are null!"); }

		Vec3 centerToUse = center != null ? center : childCenter;
		point = transformations.apply(point, centerToUse);

		return parent != null ? parent.transformPos(point, centerToUse) : point;
	}

	@Override public List<? extends BlockPos> transformBlockPos(BlockPos blockPos, boolean allowScaled)
	{
		return transformBlockPos(List.of(blockPos), null, allowScaled);
	}

	private List<BlockPos> transformBlockPos(List<BlockPos> blockPos, @Nullable Vec3 childCenter, boolean allowScaled)
	{
		if (childCenter == null && center == null) { throw new RuntimeException("Both childCenter and center are null!"); }

		Vec3 centerToUse = center != null ? center : childCenter;
		List<BlockPos> list = transformations.applyToBlockPos(blockPos, centerToUse);
		if (list.size() > 1 && !allowScaled) { return List.of(); }

		return parent != null ? parent.transformBlockPos(list, centerToUse, allowScaled) : list;
	}

	@Override public BlockState transformBlockState(BlockState blockState)
	{
		blockState = transformations.applyToBlockState(blockState);
		return parent != null ? parent.transformBlockState(blockState) : blockState;
	}

	@Override public float transformRotation(float rot)
	{
		rot = (float)transformations.applyToRotation(rot);
		return parent != null ? parent.transformRotation(rot) : rot;
	}
}
