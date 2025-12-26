package net.mt1006.mocap.mocap.playing.modifiers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.mt1006.mocap.api.v1.modifiers.MocapPlayerAsEntity;
import net.mt1006.mocap.mocap.files.SceneFiles;
import net.mt1006.mocap.utils.Utils;
import org.jetbrains.annotations.Nullable;

public class PlayerAsEntity implements MocapPlayerAsEntity
{
	public static final PlayerAsEntity DISABLED = new PlayerAsEntity(null, null);
	private final @Nullable String entityId;
	public final @Nullable String entityNbt;
	private final @Nullable EntityType<?> entityType;
	private final @Nullable CompoundTag compoundTag;

	public PlayerAsEntity(@Nullable String entityId, @Nullable String entityNbt)
	{
		this.entityId = entityId;
		this.entityNbt = entityNbt;
		this.entityType = prepareEntityType(entityId);
		this.compoundTag = prepareCompoundTag(entityNbt);
	}

	public static PlayerAsEntity fromObject(@Nullable SceneFiles.Reader reader)
	{
		return reader != null ? new PlayerAsEntity(reader.readString("id"), reader.readString("nbt")) : DISABLED;
	}

	@Override public boolean isEnabled()
	{
		return entityId != null;
	}

	@Override public @Nullable ResourceLocation getEntityId()
	{
		return entityId != null ? ResourceLocation.tryParse(entityId) : null;
	}

	@Override public @Nullable EntityType<?> getEntityType()
	{
		ResourceLocation id = getEntityId();
		if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) { return null; }
		EntityType<?> entityTypeRef = BuiltInRegistries.ENTITY_TYPE.get(id);
		return BuiltInRegistries.ENTITY_TYPE.containsKey(id) ? entityTypeRef : null;
	}

	@Override public @Nullable String getRawEntityId()
	{
		return entityId;
	}

	@Override public @Nullable String getNbt()
	{
		return entityNbt;
	}

	@Override public @Nullable SceneFiles.Writer save()
	{
		if (!isEnabled()) { return null; }

		SceneFiles.Writer writer = new SceneFiles.Writer();
		writer.addString("id", entityId);
		writer.addString("nbt", entityNbt);

		return writer;
	}

	@Override public @Nullable Entity createEntity(Level level)
	{
		if (entityType == null && compoundTag == null) { return null; }
		return (compoundTag != null)
				? EntityType.create(compoundTag, level).orElse(null)
				: entityType.create(level);
	}

	private static @Nullable EntityType<?> prepareEntityType(@Nullable String entityId)
	{
		if (entityId == null) { return null; }

		ResourceLocation entityRes = ResourceLocation.parse(entityId);
		EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityRes);
		return BuiltInRegistries.ENTITY_TYPE.containsKey(entityRes) ? entityType : null;
	}

	private static @Nullable CompoundTag prepareCompoundTag(@Nullable String entityNbt)
	{
		try
		{
			return entityNbt != null ? Utils.nbtFromString(entityNbt) : null;
		}
		catch (CommandSyntaxException e) { return null; }
	}
}
