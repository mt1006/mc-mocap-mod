package net.mt1006.mocap.mocap.playing.modifiers;

import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.mocap.files.SceneFiles;
import org.jetbrains.annotations.Nullable;

public class Rotation
{
	public static final Rotation ZERO = new Rotation(0.0);
	public final double deg;
	private final double sinDeg, cosDeg;
	public final boolean canRotateInt;
	public final net.minecraft.world.level.block.Rotation blockRotation;

	private Rotation(double deg)
	{
		deg = clampRot(deg);
		this.deg = deg;
		this.sinDeg = calcSin(deg);
		this.cosDeg = calcCos(deg);
		this.canRotateInt = (deg == 0.0 || deg == 90.0 || deg == 180.0 || deg == -90.0);

		this.blockRotation = switch ((int)Math.round(deg / 90.0))
		{
			case -2, 2 -> net.minecraft.world.level.block.Rotation.CLOCKWISE_180;
			case -1 -> net.minecraft.world.level.block.Rotation.COUNTERCLOCKWISE_90;
			case 1 -> net.minecraft.world.level.block.Rotation.CLOCKWISE_90;
			default -> net.minecraft.world.level.block.Rotation.NONE;
		};
	}

	public static Rotation fromDouble(double val)
	{
		return val != 0.0 ? new Rotation(val) : ZERO;
	}

	public static double clampRot(double deg)
	{
		if (deg > -180.0 && deg <= 180.0) { return deg; }

		// keeping deg in range (-180.0;180.0]
		double val = (deg + 180.0) % 360.0;
		if (val <= 0.0) { val += 360.0; }
		return val - 180.0;
	}

	private static double calcSin(double deg)
	{
		if (deg == 0.0 || deg == 180.0) { return 0.0; }
		else if (deg == 90.0) { return 1.0; }
		else if (deg == -90.0) { return -1.0; }
		else { return Math.sin(Math.toRadians(deg)); }
	}

	private static double calcCos(double deg)
	{
		if (deg == 0.0) { return 1.0; }
		else if (deg == 90.0 || deg == -90.0) { return 0.0; }
		else if (deg == 180.0) { return -1.0; }
		else { return Math.cos(Math.toRadians(deg)); }
	}

	public @Nullable SceneFiles.Writer save()
	{
		if (deg == 0.0) { return null; }

		SceneFiles.Writer writer = new SceneFiles.Writer();
		writer.addDouble("deg", deg, 0.0);

		return writer;
	}

	public Vec3 apply(Vec3 point, Vec3 center)
	{
		if (deg == 0.0) { return point; }

		double pointX = point.x - center.x;
		double pointZ = point.z - center.z;
		double finX = pointX * cosDeg - pointZ * sinDeg + center.x;
		double finZ = pointZ * cosDeg + pointX * sinDeg + center.z;
		return new Vec3(finX, point.y, finZ);
	}
}
