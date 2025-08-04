package net.mt1006.mocap.api.v1.modifiers;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class MocapOffset extends Vec3
{
	public static final MocapOffset ZERO = new MocapOffset(0.0, 0.0, 0.0);

	@ApiStatus.Internal
	public final boolean isZero, isInt;

	public MocapOffset(double x, double y, double z)
	{
		super(x, y, z);
		isZero = (x == 0.0 && y == 0.0 && z == 0.0);
		isInt = (isZero || (x == (int)x && y == (int)y && z == (int)z));
	}

	public static MocapOffset fromVec3(@Nullable Vec3 vec)
	{
		return vec != null ? new MocapOffset(vec.x, vec.y, vec.z) : ZERO;
	}

	@ApiStatus.Internal
	public @Nullable Vec3 save()
	{
		return isZero ? null : this;
	}

	@ApiStatus.Internal
	public Vec3 apply(Vec3 point)
	{
		return new Vec3(x + point.x, y + point.y, z + point.z);
	}
}
