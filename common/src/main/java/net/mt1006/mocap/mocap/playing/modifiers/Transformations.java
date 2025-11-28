package net.mt1006.mocap.mocap.playing.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.api.v1.modifiers.MocapMirror;
import net.mt1006.mocap.api.v1.modifiers.MocapOffset;
import net.mt1006.mocap.api.v1.modifiers.MocapTransformations;
import net.mt1006.mocap.api.v1.modifiers.MocapTransformationsConfig;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.files.SceneFiles;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Transformations implements MocapTransformations
{
	public static final Transformations EMPTY = new Transformations(Rotation.ZERO,
			MocapMirror.NONE, Scale.NORMAL, MocapOffset.ZERO, TransformationsConfig.DEFAULT);
	private final Rotation rotation;
	private final MocapMirror mirror;
	private final Scale scale;
	private final MocapOffset offset;
	private final MocapTransformationsConfig config;
	private final boolean ignorable;

	private Transformations(Rotation rotation, MocapMirror mirror, Scale scale, MocapOffset offset, MocapTransformationsConfig config)
	{
		this.rotation = rotation;
		this.mirror = mirror;
		this.scale = scale;
		this.offset = offset;
		this.config = config;
		this.ignorable = isIgnorable();
	}

	private Transformations(SceneFiles.Reader reader)
	{
		this.rotation = Rotation.fromDouble(reader.readDouble("rotation", 0.0));
		this.mirror = MocapMirror.fromString(reader.readString("mirror"));
		this.scale = Scale.fromObject(reader.readObject("scale"));
		this.offset = MocapOffset.fromVec3(reader.readVec3("offset"));
		this.config = TransformationsConfig.fromObject(reader.readObject("config"));
		this.ignorable = isIgnorable();
	}

	private boolean isIgnorable()
	{
		return (rotation.deg == 0.0 && mirror == MocapMirror.NONE && scale.sceneScale == 1.0 && offset.isZero && config.isDefault());
	}

	public static Transformations fromObject(@Nullable SceneFiles.Reader reader)
	{
		return reader != null ? new Transformations(reader) : EMPTY;
	}

	public static MocapTransformations fromLegacyScene(double x, double y, double z)
	{
		return EMPTY.withOffset(new MocapOffset(x, y, z)).withConfig(TransformationsConfig.LEGACY);
	}

	@Override public double getRotation()
	{
		return rotation.deg;
	}

	@Override public MocapTransformations withRotation(double rot)
	{
		return new Transformations(Rotation.fromDouble(rot), mirror, scale, offset, config);
	}

	@Override public MocapMirror getMirror()
	{
		return mirror;
	}

	@Override public MocapTransformations withMirror(MocapMirror mirror)
	{
		return new Transformations(rotation, mirror, scale, offset, config);
	}

	@Override public double getScaleOfPlayer()
	{
		return scale.playerScale;
	}

	@Override public MocapTransformations withScaleOfPlayer(double scale)
	{
		return new Transformations(rotation, mirror, this.scale.ofPlayer(scale), offset, config);
	}

	@Override public double getScaleOfScene()
	{
		return scale.sceneScale;
	}

	@Override public MocapTransformations withScaleOfScene(double scale)
	{
		return new Transformations(rotation, mirror, this.scale.ofScene(scale), offset, config);
	}

	@Override public MocapOffset getOffset()
	{
		return offset;
	}

	@Override public MocapTransformations withOffset(MocapOffset offset)
	{
		return new Transformations(rotation, mirror, scale, offset, config);
	}

	@Override public MocapTransformationsConfig getConfig()
	{
		return config;
	}

	@Override public MocapTransformations withConfig(MocapTransformationsConfig config)
	{
		return new Transformations(rotation, mirror, scale, offset, config);
	}

	@Override public Vec3 calculateCenter(Vec3 startPos)
	{
		Vec3 center = calculateCenterWithoutOffset(startPos);
		return config.getCenterOffset().isZero ? center : center.add(config.getCenterOffset());
	}

	private Vec3 calculateCenterWithoutOffset(Vec3 startPos)
	{
		if (config.getRecordingCenter() != TransformationsConfig.RecordingCenter.AUTO)
		{
			return switch (config.getRecordingCenter())
			{
				case BLOCK_CENTER -> getBlockCenter(startPos);
				case BLOCK_CORNER -> getBlockCorner(startPos);
				case ACTUAL -> startPos;
				default -> throw new IllegalStateException("Unexpected config.centerPoint value");
			};
		}

		if (scale.sceneScale == 1 || scale.sceneScale != (int)scale.sceneScale)
		{
			Vec3 blockCenter = getBlockCenter(startPos);
			Vec3 blockCorner = getBlockCorner(startPos);

			return (startPos.distanceToSqr(blockCenter) > startPos.distanceToSqr(blockCorner)) ? blockCorner : blockCenter;
		}
		else
		{
			return ((int)scale.sceneScale % 2 == 1) ? getBlockCenter(startPos) : getBlockCorner(startPos);
		}
	}

	private static Vec3 getBlockCenter(Vec3 vec)
	{
		return new Vec3((double)Math.round(vec.x - 0.5) + 0.5, Math.floor(vec.y), (double)Math.round(vec.z - 0.5) + 0.5);
	}

	private static Vec3 getBlockCorner(Vec3 vec)
	{
		return new Vec3((double)Math.round(vec.x), Math.floor(vec.y), (double)Math.round(vec.z));
	}

	@Override public Vec3 apply(Vec3 point, Vec3 center)
	{
		if (ignorable) { return point; }

		point = rotation.apply(point, center);
		point = mirror.apply(point, center);
		point = scale.applyToPoint(point, center);
		point = offset.apply(point);

		return point;
	}

	@Override public List<? extends BlockPos> applyToBlockPos(List<? extends BlockPos> inputList, Vec3 center)
	{
		if (ignorable) { return inputList; }
		if (inputList.size() == 1) { return applyToBlockPos(inputList.get(0), center); }

		List<BlockPos> list = new ArrayList<>(inputList.size());
		inputList.forEach((b) -> list.addAll(applyToBlockPos(b, center)));
		return list;
	}

	private List<BlockPos> applyToBlockPos(BlockPos blockPos, Vec3 center)
	{
		if (!config.getRoundBlockPos() && (!isIntVec(center.multiply(2.0, 2.0, 2.0))
				|| !rotation.canRotateInt || !scale.canScaleInt(center) || !offset.isInt))
		{
			return List.of();
		}

		Vec3 point1 = Vec3.atLowerCornerOf(blockPos);
		Vec3 point2 = point1.add(1.0, 1.0, 1.0);
		point1 = apply(point1, center);
		point2 = apply(point2, center);

		return voxelizeCube(point1, point2);
	}

	private List<BlockPos> voxelizeCube(Vec3 pos1, Vec3 pos2)
	{
		if (isIntVec(pos1) && (pos1.x + 1.0 == pos2.x) && (pos1.y + 1.0 == pos2.y) && (pos1.z + 1.0 == pos2.z))
		{
			return List.of(new BlockPos((int)pos1.x, (int)pos1.y, (int)pos1.z));
		}

		int startY = (int)Math.round(pos1.y);
		int stopY = (int)Math.round(pos2.y);

		if (Math.abs(pos1.x - pos2.x) == Math.abs(pos1.z - pos2.z)) // if square isn't rotated
		{
			int startX = (int)Math.round(Math.min(pos1.x, pos2.x));
			int stopX = (int)Math.round(Math.max(pos1.x, pos2.x));
			int startZ = (int)Math.round(Math.min(pos1.z, pos2.z));
			int stopZ = (int)Math.round(Math.max(pos1.z, pos2.z));

			List<BlockPos> list = new ArrayList<>((stopX - startX) * (stopZ - startZ) * (stopY - startY));
			for (int y = startY; y < stopY; y++)
			{
				for (int z = startZ; z < stopZ; z++)
				{
					for (int x = startX; x < stopX; x++)
					{
						list.add(new BlockPos(x, y, z));
					}
				}
			}
			return list;
		}
		else
		{
			double bottomY = pos1.y;
			double sqCenterX = (pos1.x + pos2.x) / 2.0;
			double sqCenterZ = (pos1.z + pos2.z) / 2.0;
			Vec3 pos3 = new Vec3(sqCenterX + (pos1.z - sqCenterZ), bottomY, sqCenterZ - (pos1.x - sqCenterX));
			Vec3 pos4 = new Vec3(sqCenterX + (pos2.z - sqCenterZ), bottomY, sqCenterZ - (pos2.x - sqCenterX));
			int minZ = (int)Math.round(Math.min(Math.min(pos1.z, pos2.z), Math.min(pos3.z, pos4.z)));
			int maxZ = (int)Math.round(Math.max(Math.max(pos1.z, pos2.z), Math.max(pos3.z, pos4.z)));

			Vec3[] vertices = {pos1, pos3, pos2, pos4};
			ArrayList<Integer> nodesX = new ArrayList<>(4);

			List<BlockPos> list = new ArrayList<>();
			for (int z = minZ; z <= maxZ; z++)
			{
				nodesX.clear();
				int j = vertices.length - 1;
				for (int i = 0; i < vertices.length; i++)
				{
					Vec3 v1 = vertices[i], v2 = vertices[j];
					if ((z > v1.z && z < v2.z) || (z > v2.z && z < v1.z))
					{
						double x = v1.x + ((v2.x - v1.x) * ((z - v1.z)/(v2.z - v1.z)));
						nodesX.add((int)Math.round(x));
					}
					j = i;
				}

				if (nodesX.isEmpty()) { continue; }

				int startX = Collections.min(nodesX);
				int stopX = Collections.max(nodesX);
				for (int x = startX; x < stopX; x++)
				{
					for (int y = startY; y < stopY; y++)
					{
						list.add(new BlockPos(x, y, z));
					}
				}
			}
			return list;
		}
	}

	private static boolean isIntVec(Vec3 vec)
	{
		return vec.x == (int)vec.x && vec.y == (int)vec.y && vec.z == (int)vec.z;
	}

	@Override public BlockState applyToBlockState(BlockState blockState)
	{
		if (ignorable) { return blockState; }

		if (rotation.deg != 0.0) { blockState = blockState.rotate(rotation.blockRotation); }
		if (mirror.mirrorX) { blockState = blockState.mirror(Mirror.FRONT_BACK); }
		if (mirror.mirrorZ) { blockState = blockState.mirror(Mirror.LEFT_RIGHT); }
		return blockState;
	}

	@Override public double applyToRotation(double rot)
	{
		if (ignorable) { return rot; }

		rot += rotation.deg;
		if (mirror.mirrorX) { rot = -rot; }
		if (mirror.mirrorZ) { rot = -(rot + 90.0) - 90.0; }
		return Rotation.clampRot(rot);
	}

	@Override public void applyScaleToPlayer(Entity entity)
	{
		scale.applyToPlayer(entity);
	}

	@Override public void applyScaleToEntity(Entity entity)
	{
		scale.applyToEntity(entity);
	}

	@Override public boolean areDefault()
	{
		return rotation.deg == 0.0 && mirror == MocapMirror.NONE && scale.isNormal() && offset.isZero && config.isDefault();
	}

	@Override public MocapTransformations mergeWithParent(MocapTransformations parent)
	{
		// it only changes player scale as parent position transformations are applied by PositionTransformer
		return withScaleOfPlayer(scale.playerScale * parent.getScaleOfPlayer());
	}

	@Override public @Nullable SceneFiles.Writer save()
	{
		if (areDefault()) { return null; }

		SceneFiles.Writer writer = new SceneFiles.Writer();
		writer.addDouble("rotation", rotation.deg, 0.0);
		writer.addString("mirror", mirror.save());
		writer.addVec3("offset", offset.save());
		writer.addObject("scale", scale.save());
		writer.addObject("config", config.save());

		return writer;
	}

	@Override public void list(CommandOutput out)
	{
		out.sendSuccess("scenes.element_info.transformations.rotation", rotation.deg);
		out.sendSuccess("scenes.element_info.transformations.mirror." + mirror.name().toLowerCase(Locale.ROOT));

		if (scale.playerScale == 1.0) {out.sendSuccess("scenes.element_info.transformations.player_scale.normal");}
		else {out.sendSuccess("scenes.element_info.transformations.player_scale.custom", scale.playerScale);}

		if (scale.sceneScale == 1.0) {out.sendSuccess("scenes.element_info.transformations.scene_scale.normal");}
		else {out.sendSuccess("scenes.element_info.transformations.scene_scale.custom", scale.sceneScale);}

		out.sendSuccess("scenes.element_info.transformations.offset", offset.x, offset.y, offset.z);
		config.list(out);
	}

	@Override public @Nullable MocapTransformations modify(FullCommandInfo info, int propertyNodePos)
	{
		switch (info.getNode(propertyNodePos))
		{
			case "rotation":
				return withRotation(info.getDouble("deg"));

			case "mirror":
				MocapMirror newMirror = MocapMirror.fromStringOrNull(info.getNode(propertyNodePos + 1));
				return newMirror != null ? withMirror(newMirror) : null;

			case "scale":
				double scaleVal = info.getDouble("scale");
				return switch (info.getNode(propertyNodePos + 1))
				{
					case "of_player" -> withScaleOfPlayer(scaleVal);
					case "of_scene" -> withScaleOfScene(scaleVal);
					case null, default -> null;
				};

			case "offset":
				return withOffset(new MocapOffset(info.getDouble("offset_x"), info.getDouble("offset_y"), info.getDouble("offset_z")));

			case "config":
				MocapTransformationsConfig newConfig = config.modify(info, propertyNodePos + 1);
				return newConfig != null ? withConfig(newConfig) : null;

			case null, default:
				return null;
		}
	}
}
