package net.mt1006.mocap.mocap.playing.modifiers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.mt1006.mocap.api.v1.modifiers.MocapPlayerAsEntity;
import net.mt1006.mocap.mocap.files.SceneFiles;
import net.mt1006.mocap.utils.Utils;
import org.jetbrains.annotations.Nullable;

public class PlayerAsEntity implements MocapPlayerAsEntity
{
	public static final PlayerAsEntity DISABLED = new PlayerAsEntity(null, null);

	public final @Nullable String entityId;
	public final @Nullable String entityNbt;
	private final @Nullable EntityType<?> entityType;
	private final @Nullable CompoundTag compoundTag;

	public PlayerAsEntity(@Nullable String entityId, @Nullable String entityNbt)
	{
		this.entityId = entityId;
		this.entityNbt = entityNbt;
		this.entityType = prepareEntityType(entityId, entityNbt);
		this.compoundTag = prepareCompoundTag(entityId, entityNbt);
	}

	public PlayerAsEntity(@Nullable SceneFiles.Reader reader)
	{
		if (reader == null)
		{
			entityId = null;
			entityNbt = null;
			entityType = null;
			compoundTag = null;
			return;
		}

		entityId = reader.readString("id");
		entityNbt = reader.readString("nbt");
		this.entityType = prepareEntityType(entityId, entityNbt);
		this.compoundTag = prepareCompoundTag(entityId, entityNbt);
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
		Holder.Reference<EntityType<?>> entityTypeRef = BuiltInRegistries.ENTITY_TYPE.get(id).orElse(null);
		return entityTypeRef != null ? entityTypeRef.value() : null;
	}

	@Override public @Nullable String getNbt()
	{
		return entityNbt;
	}

	public @Nullable SceneFiles.Writer save()
	{
		if (!isEnabled()) { return null; }

		SceneFiles.Writer writer = new SceneFiles.Writer();
		writer.addString("id", entityId);
		writer.addString("nbt", entityNbt);

		return writer;
	}

	public @Nullable Entity createEntity(Level level)
	{
		if (entityType == null && compoundTag == null) { return null; }
		return (compoundTag != null)
				? EntityType.create(compoundTag, level, EntitySpawnReason.COMMAND).orElse(null)
				: entityType.create(level, EntitySpawnReason.COMMAND);
	}

	private static @Nullable EntityType<?> prepareEntityType(@Nullable String entityId, @Nullable String entityNbt)
	{
		if (entityId == null || entityNbt != null) { return null; } // if entityNbt is present, it should be null

		ResourceLocation entityRes = ResourceLocation.parse(entityId);
		Holder.Reference<EntityType<?>> entityTypeRef = BuiltInRegistries.ENTITY_TYPE.get(entityRes).orElse(null);
		return entityTypeRef != null ? entityTypeRef.value() : null;
	}

	private static @Nullable CompoundTag prepareCompoundTag(@Nullable String entityId, @Nullable String entityNbt)
	{
		if (entityId == null || entityNbt == null) { return null; }

		try
		{
			CompoundTag nbt = Utils.nbtFromString(entityNbt);
			nbt.putString("id", entityId);
			return nbt;
		}
		catch (CommandSyntaxException e) { return null; }
	}
}
