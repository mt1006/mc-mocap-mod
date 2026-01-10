package net.mt1006.mocap.api.v1.modifiers;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface MocapEntityFilter
{
	boolean isAllowed(Entity entity);

	boolean isEmpty();

	@Nullable String getFilterString();

	@ApiStatus.Internal
	@Nullable String save();
}
