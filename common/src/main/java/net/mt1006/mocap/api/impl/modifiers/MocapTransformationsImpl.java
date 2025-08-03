package net.mt1006.mocap.api.impl.modifiers;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.modifiers.MocapMirror;
import net.mt1006.mocap.api.v1.modifiers.MocapOffset;
import net.mt1006.mocap.api.v1.modifiers.MocapTransformations;
import net.mt1006.mocap.api.v1.modifiers.MocapTransformationsConfig;
import net.mt1006.mocap.mocap.playing.modifiers.Rotation;
import net.mt1006.mocap.mocap.playing.modifiers.Transformations;

import java.util.function.Consumer;

public class MocapTransformationsImpl implements MocapTransformations
{
	private final Transformations transformations;

	private MocapTransformationsImpl(Transformations transformations)
	{
		this.transformations = transformations;
	}

	public static MocapTransformationsImpl ofCopy(Transformations transformations)
	{
		return new MocapTransformationsImpl(transformations.copy());
	}

	@Override public Transformations getCopy()
	{
		return transformations.copy();
	}

	private MocapTransformationsImpl modify(Consumer<Transformations> modifier)
	{
		Transformations copy = transformations.copy();
		modifier.accept(copy);
		return new MocapTransformationsImpl(copy);
	}

	@Override public double getRotation()
	{
		return transformations.rotation.deg;
	}

	@Override public MocapTransformations setRotation(double rot)
	{
		return modify((t) -> t.rotation = new Rotation(rot));
	}

	@Override public MocapMirror getMirror()
	{
		return transformations.mirror;
	}

	@Override public MocapTransformations setMirror(MocapMirror mirror)
	{
		return modify((t) -> t.mirror = mirror);
	}

	@Override public double getScaleOfPlayer()
	{
		return transformations.scale.playerScale;
	}

	@Override public MocapTransformations setScaleOfPlayer(double scale)
	{
		return modify((t) -> t.scale = t.scale.ofPlayer(scale));
	}

	@Override public double getScaleOfScene()
	{
		return transformations.scale.sceneScale;
	}

	@Override public MocapTransformations setScaleOfScene(double scale)
	{
		return modify((t) -> t.scale = t.scale.ofScene(scale));
	}

	@Override public Vec3 getOffset()
	{
		return transformations.offset;
	}

	@Override public MocapTransformations setOffset(Vec3 offset)
	{
		return modify((t) -> t.offset = new MocapOffset(offset.x, offset.y, offset.z));
	}

	@Override public MocapTransformationsConfig getConfig()
	{
		return transformations.config;
	}

	@Override public MocapTransformations setConfig(MocapTransformationsConfig config)
	{
		return modify((t) -> t.config = config);
	}

	@Override public void applyScaleToEntity(Entity entity)
	{
		transformations.scale.applyToEntity(entity);
	}
}
