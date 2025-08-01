package net.mt1006.mocap.api.v1.modifiers;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public enum MocapMirror
{
	NONE(false, false),
	X(true, false),
	Z(false, true),
	XZ(true, true);

	@ApiStatus.Internal
	public final boolean mirrorX, mirrorZ;

	MocapMirror(boolean mirrorX, boolean mirrorZ)
	{
		this.mirrorX = mirrorX;
		this.mirrorZ = mirrorZ;
	}

	@ApiStatus.Internal
	public static MocapMirror fromString(@Nullable String str)
	{
		try
		{
			return str != null ? MocapMirror.valueOf(str.toUpperCase()) : NONE;
		}
		catch (IllegalArgumentException e) { return NONE; }
	}

	@ApiStatus.Internal
	public static @Nullable MocapMirror fromStringOrNull(@Nullable String str)
	{
		try
		{
			return str != null ? MocapMirror.valueOf(str.toUpperCase()) : null;
		}
		catch (IllegalArgumentException e) { return null; }
	}

	@ApiStatus.Internal
	public @Nullable String save()
	{
		return this == NONE ? null : name().toLowerCase();
	}

	@ApiStatus.Internal
	public Vec3 apply(Vec3 point, Vec3 center)
	{
		if (this != NONE)
		{
			double x = mirrorX ? (-(point.x - center.x) + center.x) : point.x;
			double z = mirrorZ ? (-(point.z - center.z) + center.z) : point.z;
			return new Vec3(x, point.y, z);
		}
		return point;
	}
}
