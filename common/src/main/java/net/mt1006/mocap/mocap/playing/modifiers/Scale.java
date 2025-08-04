package net.mt1006.mocap.mocap.playing.modifiers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.mocap.files.SceneFiles;
import org.jetbrains.annotations.Nullable;

public class Scale
{
	public static final Scale NORMAL = new Scale(1.0, 1.0);
	private static final @Nullable ResourceLocation SCALE_ID = BuiltInRegistries.ATTRIBUTE.getKey(Attributes.SCALE.value());
	public final double playerScale, sceneScale, totalSceneScale;

	private Scale(double playerScale, double sceneScale, double totalSceneScale)
	{
		this.playerScale = playerScale;
		this.sceneScale = sceneScale;
		this.totalSceneScale = totalSceneScale;
	}

	private Scale(double playerScale, double sceneScale)
	{
		this(playerScale, sceneScale, sceneScale);
	}

	public static Scale fromObject(@Nullable SceneFiles.Reader reader)
	{
		return reader != null
				? new Scale(reader.readDouble("player_scale", 1.0), reader.readDouble("scene_scale", 1.0))
				: NORMAL;
	}

	public Scale ofPlayer(double scale)
	{
		return new Scale(scale, sceneScale);
	}

	public Scale ofScene(double scale)
	{
		return new Scale(playerScale, scale);
	}

	public @Nullable SceneFiles.Writer save()
	{
		if (isNormal()) { return null; }

		SceneFiles.Writer writer = new SceneFiles.Writer();
		writer.addDouble("player_scale", playerScale, 1.0);
		writer.addDouble("scene_scale", sceneScale, 1.0);

		return writer;
	}

	public boolean isNormal()
	{
		return (playerScale == 1.0 && sceneScale == 1.0);
	}

	public boolean canScaleInt(Vec3 startPos)
	{
		if (sceneScale == 1.0) { return true; }
		if (sceneScale != (int)sceneScale) { return false; }

		Vec3 vec = ((int)sceneScale % 2 == 0) ? startPos : startPos.add(0.5, 0.0, 0.5);
		return vec.x == (int)vec.x && vec.y == (int)vec.y && vec.z == (int)vec.z;
	}

	public void applyToPlayer(Entity player)
	{
		applyToEntity(player, playerScale * totalSceneScale);
	}

	public void applyToEntity(Entity entity)
	{
		applyToEntity(entity, totalSceneScale);
	}

	private void applyToEntity(Entity entity, double scale)
	{
		if (scale == 1.0 || !(entity instanceof LivingEntity) || SCALE_ID == null) { return; }

		AttributeModifier modifier = new AttributeModifier(SCALE_ID, scale - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		AttributeInstance instance = ((LivingEntity)entity).getAttributes().getInstance(Attributes.SCALE);
		if (instance != null) { instance.addPermanentModifier(modifier); }
	}

	public Vec3 applyToPoint(Vec3 point, Vec3 center)
	{
		if (sceneScale == 1.0) { return point; }

		return new Vec3(((point.x - center.x) * sceneScale) + center.x,
				((point.y - center.y) * sceneScale) + center.y,
				((point.z - center.z) * sceneScale) + center.z);
	}
}
