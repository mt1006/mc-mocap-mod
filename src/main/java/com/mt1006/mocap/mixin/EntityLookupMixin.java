package com.mt1006.mocap.mixin;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

@Mixin(EntityLookup.class)
public class EntityLookupMixin<T extends EntityAccess>
{
	@Shadow private final Int2ObjectMap<T> byId = new Int2ObjectLinkedOpenHashMap<>();
	@Shadow private final Map<UUID, T> byUuid = Maps.newHashMap();

	@Inject(at = @At(value = "HEAD"), method = "add", cancellable = true)
	protected void atAdd(T toAdd, CallbackInfo callbackInfo)
	{
		// Skips UUID duplicate check
		callbackInfo.cancel();
		this.byUuid.put(toAdd.getUUID(), toAdd);
		this.byId.put(toAdd.getId(), toAdd);
	}
}
