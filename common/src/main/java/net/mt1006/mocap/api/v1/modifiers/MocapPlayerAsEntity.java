package net.mt1006.mocap.api.v1.modifiers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.mt1006.mocap.mocap.files.SceneFiles;
import net.mt1006.mocap.mocap.playing.modifiers.PlayerAsEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface MocapPlayerAsEntity
{
	MocapPlayerAsEntity DISABLED = new PlayerAsEntity(null, null);

	static MocapPlayerAsEntity create(EntityType<?> entityType, @Nullable String nbt)
	{
		return create(BuiltInRegistries.ENTITY_TYPE.getKey(entityType), nbt);
	}

	static MocapPlayerAsEntity create(Identifier id, @Nullable String nbt)
	{
		return new PlayerAsEntity(id.toString(), nbt);
	}

	boolean isEnabled();

	@Nullable Identifier getEntityId();

	@Nullable EntityType<?> getEntityType();

	@Nullable String getRawEntityId();

	@Nullable String getNbt();

	@ApiStatus.Internal
	@Nullable SceneFiles.Writer save();

	@ApiStatus.Internal
	@Nullable Entity createEntity(Level level);
}
