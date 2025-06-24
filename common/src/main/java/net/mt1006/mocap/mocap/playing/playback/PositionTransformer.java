package net.mt1006.mocap.mocap.playing.playback;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.extension.MocapPositionTransformer;
import net.mt1006.mocap.mocap.playing.modifiers.Transformations;
import net.mt1006.mocap.mocap.settings.Settings;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PositionTransformer implements MocapPositionTransformer
{
	private final Transformations transformations;
	private final @Nullable PositionTransformer parent;
	private final @Nullable Vec3 center;

	public PositionTransformer(Transformations transformations, @Nullable PositionTransformer parent, @Nullable Vec3 center)
	{
		this.parent = getProperParent(parent, transformations);
		this.transformations = transformations;
		this.center = center;
	}

	private static @Nullable PositionTransformer getProperParent(@Nullable PositionTransformer parent, Transformations transformations)
	{
		if (parent != null && transformations.parent != parent.transformations) { throw new RuntimeException(); }
		return (parent == null && transformations.parent != null) ? new PositionTransformer(transformations.parent, null, null) : parent;
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

	@Override public List<? extends BlockPos> transformBlockPos(BlockPos blockPos)
	{
		return transformBlockPos(List.of(blockPos), null);
	}

	private List<BlockPos> transformBlockPos(List<BlockPos> blockPos, @Nullable Vec3 childCenter)
	{
		if (childCenter == null && center == null) { throw new RuntimeException("Both childCenter and center are null!"); }

		Vec3 centerToUse = center != null ? center : childCenter;
		List<BlockPos> list = transformations.applyToBlockPos(blockPos, centerToUse);
		if (list.size() > 1 && !Settings.BLOCK_ALLOW_SCALED.val) { return List.of(); }

		return parent != null ? parent.transformBlockPos(list, centerToUse) : list;
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
