package net.mt1006.mocap.mocap.actions;

import com.mojang.serialization.DynamicOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.v1.MocapAPI;
import net.mt1006.mocap.api.v1.extension.MocapExtension;
import net.mt1006.mocap.api.v1.extension.MocapRecordingContext;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.mocap.settings.Settings;
import net.mt1006.mocap.utils.Utils;
import org.jetbrains.annotations.Nullable;

/** Records and plays a successful pickup of an already tracked item entity. */
public class ItemPickupAction implements MocapAction
{
	private static final String EXTENSION_ID = "mocap_replay_item_pickups";
	private static final short EXTENSION_VERSION = 1;
	private static final int ACTION_ID = 0;
	private static @Nullable MocapExtension extension;
	private static boolean extensionRegistrationAttempted;

	private final int entityId;
	private final int amount;
	private final String originalStack;
	private final String remainingStack;

	private ItemPickupAction(int entityId, int amount, String originalStack, String remainingStack)
	{
		this.entityId = entityId;
		this.amount = amount;
		this.originalStack = originalStack;
		this.remainingStack = remainingStack;
	}

	public ItemPickupAction(Reader reader, MocapRecordingData data)
	{
		entityId = reader.readInt();
		amount = reader.readInt();
		originalStack = reader.readString();
		remainingStack = reader.readString();
	}

	public static void registerExtension()
	{
		if (extensionRegistrationAttempted) { return; }
		extensionRegistrationAttempted = true;

		MocapExtension registered = MocapAPI.registerExtension(EXTENSION_ID, EXTENSION_VERSION);
		if (registered == null)
		{
			MocapMod.LOGGER.warn("Unable to register optional item-pickup recording extension; pickup recording is disabled.");
			return;
		}

		try
		{
			registered.registerAction(ACTION_ID, ItemPickupAction::new);
			extension = registered;
		}
		catch (RuntimeException exception)
		{
			MocapMod.LOGGER.error("Unable to register optional item-pickup recording action; pickup recording is disabled.", exception);
		}
	}

	public static void record(Player player, ItemEntity item, ItemStack original, ItemStack remaining)
	{
		MocapExtension currentExtension = extension;
		if (currentExtension == null || !currentExtension.isRecordingActive()) { return; }

		for (MocapRecordingContext context : currentExtension.findRecordingByRecordedPlayer(player))
		{
			if (context.getState() != MocapRecordingContext.State.RECORDING) { continue; }

			MocapRecordingContext.TrackedEntity trackedEntity = context.getTrackedEntity(item);
			if (trackedEntity == null || trackedEntity.getEntity() != item) { continue; }

			ItemPickupAction action = create(trackedEntity.getId(), original, remaining, item.registryAccess());
			if (action != null) { context.addAction(action); }
		}
	}

	private static @Nullable ItemPickupAction create(int entityId, ItemStack original, ItemStack remaining, RegistryAccess registryAccess)
	{
		int amount = original.getCount() - remaining.getCount();
		if (original.isEmpty() || amount <= 0 || remaining.getCount() != original.getCount() - amount
				|| (!remaining.isEmpty() && !ItemStack.isSameItemSameComponents(original, remaining)))
		{
			return null;
		}

		try
		{
			return new ItemPickupAction(entityId, amount, encode(original, registryAccess), encode(remaining, registryAccess));
		}
		catch (Exception exception)
		{
			MocapMod.LOGGER.warn("Unable to serialize a successful item pickup; skipping it.", exception);
			return null;
		}
	}

	private static String encode(ItemStack stack, RegistryAccess registryAccess)
	{
		if (stack.isEmpty()) { return ""; }
		DynamicOps<Tag> ops = registryAccess.createSerializationContext(NbtOps.INSTANCE);
		return ItemStack.CODEC.encodeStart(ops, stack).getOrThrow().toString();
	}

	private static @Nullable ItemStack decode(String encoded, RegistryAccess registryAccess)
	{
		if (encoded.isEmpty()) { return ItemStack.EMPTY; }

		try
		{
			DynamicOps<Tag> ops = registryAccess.createSerializationContext(NbtOps.INSTANCE);
			return ItemStack.CODEC.parse(ops, Utils.nbtFromString(encoded)).getOrThrow();
		}
		catch (Exception exception)
		{
			return null;
		}
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addInt(entityId);
		writer.addInt(amount);
		writer.addString(originalStack);
		writer.addString(remainingStack);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (!Settings.REPLAY_ITEM_PICKUPS.val) { return Result.IGNORED; }

		MocapActionContext.EntityData entityData = ctx.getEntityData(entityId);
		if (entityData == null || !(entityData.entity instanceof ItemEntity item)
				|| item.isRemoved() || !item.isAlive() || item.level() != ctx.getLevel()
				|| ctx.getLevel().getEntity(item.getId()) != item)
		{
			return Result.IGNORED;
		}

		Entity mainEntity = ctx.getMainEntity();
		if (!(mainEntity instanceof LivingEntity player) || player.isRemoved() || !player.isAlive()
				|| player.level() != ctx.getLevel())
		{
			return Result.IGNORED;
		}

		RegistryAccess registryAccess = ctx.getLevel().registryAccess();
		ItemStack original = decode(originalStack, registryAccess);
		ItemStack remaining = decode(remainingStack, registryAccess);
		if (original == null || remaining == null || original.isEmpty() || amount <= 0
				|| original.getCount() - amount != remaining.getCount()
				|| (!remaining.isEmpty() && !ItemStack.isSameItemSameComponents(original, remaining))
				|| !ItemStack.matches(item.getItem(), original))
		{
			return Result.IGNORED;
		}

		// The packet makes clients animate and shrink by amount; set the server stack to the absolute remainder.
		player.take(item, amount);
		if (!remaining.isEmpty()) { item.setItem(remaining.copy()); }
		return Result.OK;
	}
}
