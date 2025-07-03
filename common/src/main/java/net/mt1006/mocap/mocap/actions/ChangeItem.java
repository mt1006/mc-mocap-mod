package net.mt1006.mocap.mocap.actions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapStateAction;
import net.mt1006.mocap.mixin.fields.LivingEntityFields;
import net.mt1006.mocap.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ChangeItem implements MocapStateAction
{
	private static final int ITEM_COUNT_LEGACY = 6;
	private static final int ITEM_COUNT = 7;
	private final byte itemCount;
	private final List<ItemData> items = new ArrayList<>();

	public ChangeItem(Entity entity)
	{
		if (!(entity instanceof LivingEntity))
		{
			itemCount = 0;
			for (int i = 0; i < 7; i++) { items.add(ItemData.EMPTY); }
			return;
		}
		LivingEntity livingEntity = (LivingEntity)entity;

		addItem(livingEntity.getMainHandItem(), entity);
		addItem(livingEntity.getOffhandItem(), entity);
		addItem(livingEntity.getItemBySlot(EquipmentSlot.FEET), entity);
		addItem(livingEntity.getItemBySlot(EquipmentSlot.LEGS), entity);
		addItem(livingEntity.getItemBySlot(EquipmentSlot.CHEST), entity);
		addItem(livingEntity.getItemBySlot(EquipmentSlot.HEAD), entity);
		addItem(livingEntity.getItemBySlot(EquipmentSlot.BODY), entity);

		int itemCounter = 0;
		for (int i = 0; i < ITEM_COUNT; i++)
		{
			if (items.get(i).type != ItemDataType.NO_ITEM) { itemCounter = i + 1; }
		}
		itemCount = (byte)itemCounter;
	}

	public ChangeItem(Reader reader, MocapRecordingData data)
	{
		byte firstByte = reader.readByte();

		if (firstByte >= 0)
		{
			// backward compatibility
			reader.shift(-1);
			itemCount = ITEM_COUNT_LEGACY;
		}
		else
		{
			itemCount = (byte)(firstByte != Byte.MIN_VALUE ? -firstByte : 0);
		}

		if (itemCount > ITEM_COUNT)
		{
			// Shouldn't happen, unless loading recording from newer mc version
			for (int i = 0; i < ITEM_COUNT; i++) { items.add(new ItemData(reader, data)); }
			for (int i = ITEM_COUNT; i < itemCount; i++) { new ItemData(reader, data); }
		}
		else
		{
			for (int i = 0; i < itemCount; i++) { items.add(new ItemData(reader, data)); }
			for (int i = itemCount; i < ITEM_COUNT; i++) { items.add(ItemData.EMPTY); }
		}
	}

	private void addItem(@Nullable ItemStack itemStack, Entity entity)
	{
		items.add(ItemData.get(itemStack, entity.registryAccess()));
	}

	private void setEntityItems(LivingEntity entity)
	{
		for (int i = 0; i < ITEM_COUNT; i++)
		{
			ItemData item = items.get(i);
			ItemStack itemStack = item.getItemStack(entity.registryAccess());

			switch (i)
			{
				case 0 -> entity.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
				case 1 -> entity.setItemSlot(EquipmentSlot.OFFHAND, itemStack);
				case 2 -> entity.setItemSlot(EquipmentSlot.FEET, itemStack);
				case 3 -> entity.setItemSlot(EquipmentSlot.LEGS, itemStack);
				case 4 -> entity.setItemSlot(EquipmentSlot.CHEST, itemStack);
				case 5 -> entity.setItemSlot(EquipmentSlot.HEAD, itemStack);
				case 6 -> entity.setItemSlot(EquipmentSlot.BODY, itemStack);
			}

			// for non-player living entities it's detected in their "tick" method
			if (entity instanceof Player) { ((LivingEntityFields)entity).callDetectEquipmentUpdates(); }
		}
	}

	@Override public boolean differs(MocapStateAction previousAction)
	{
		if (items.size() != ((ChangeItem)previousAction).items.size()) { return true; }

		for (int i = 0; i < items.size(); i++)
		{
			ItemData item1 = items.get(i);
			ItemData item2 = ((ChangeItem)previousAction).items.get(i);
			if (item1.differs(item2)) { return true; }
		}
		return false;
	}

	@Override public void prepareWrite(MocapRecordingData data)
	{
		if (itemCount > items.size()) { throw new RuntimeException(); }

		for (int i = 0; i < itemCount; i++)
		{
			items.get(i).prepareWrite(data);
		}
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addByte(itemCount > 0 ? (byte)(-itemCount) : Byte.MIN_VALUE);

		for (int i = 0; i < itemCount; i++)
		{
			items.get(i).write(writer);
		}
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (items.size() != ITEM_COUNT)
		{
			MocapMod.LOGGER.error("Item list size doesn't match proper item count!");
			return Result.ERROR;
		}

		LivingEntity livingEntity = ctx.getLivingEntityOrDummyPlayer();
		if (livingEntity == null) { return Result.IGNORED; }

		setEntityItems(livingEntity);
		return Result.OK;
	}

	private enum ItemDataType
	{
		NO_ITEM(0, false, false),
		ID_ONLY(1, true, false),
		ID_AND_NBT(2, true, true),
		ID_AND_COMPONENTS(3, true, true);

		private static final ItemDataType[] VALUES = values();
		public final byte id;
		public final boolean hasId;
		public final boolean hasData;

		ItemDataType(int id, boolean hasId, boolean hasData)
		{
			this.id = (byte)id;
			this.hasId = hasId;
			this.hasData = hasData;
		}

		public static ItemDataType get(byte id)
		{
			if (id < 0 || id >= VALUES.length) { return NO_ITEM; }

			ItemDataType type = VALUES[id];
			if (type.id != id) { throw new RuntimeException("ChangeItem.ItemDataType VALUES out of order!"); }
			return type;
		}
	}

	private static class ItemData
	{
		public static final ItemData EMPTY = new ItemData();
		public final ItemDataType type;
		public final Item item;
		public final String data;
		private int idToWrite = -1;

		private ItemData()
		{
			type = ItemDataType.NO_ITEM;
			item = Items.AIR;
			data = "";
		}

		private ItemData(ItemStack itemStack, RegistryAccess registryAccess)
		{
			item = itemStack.getItem();
			Tag tag = itemStack.save(registryAccess);

			if (!(tag instanceof CompoundTag) || !((CompoundTag)tag).contains("components"))
			{
				type = ItemDataType.ID_ONLY;
				data = "";
				return;
			}

			Tag componentsTag = ((CompoundTag)tag).get("components");
			if (!(componentsTag instanceof CompoundTag))
			{
				type = ItemDataType.ID_ONLY;
				data = "";
				return;
			}

			type = ItemDataType.ID_AND_COMPONENTS;
			data = componentsTag.toString();
		}

		public ItemData(Reader reader, MocapRecordingData recordingData)
		{
			type = ItemDataType.get(reader.readByte());
			int itemId = type.hasId ? reader.readInt() : 0;
			data = type.hasData ? reader.readString() : "";

			if (recordingData == null)
			{
				item = Items.AIR;
				return;
			}

			item = recordingData.itemFromId(itemId);
		}

		public static ItemData get(@Nullable ItemStack itemStack, RegistryAccess registryAccess)
		{
			return (itemStack == null || itemStack.isEmpty()) ? EMPTY : new ItemData(itemStack, registryAccess);
		}

		public boolean differs(ItemData itemData)
		{
			return type != itemData.type || item != itemData.item || !data.equals(itemData.data);
		}

		public void prepareWrite(MocapRecordingData recordingData)
		{
			idToWrite = recordingData.provideItemId(item);
		}

		public void write(Writer writer)
		{
			if (idToWrite == -1) { throw new RuntimeException("ItemData write wasn't prepared!"); }

			writer.addByte(type.id);
			if (type.hasId) { writer.addInt(idToWrite); }
			if (type.hasData) { writer.addString(data); }
		}

		public ItemStack getItemStack(RegistryAccess registryAccess)
		{
			switch (type)
			{
				case NO_ITEM:
					return ItemStack.EMPTY;

				case ID_ONLY:
				case ID_AND_NBT:
					return new ItemStack(item);

				case ID_AND_COMPONENTS:
					CompoundTag tag = tagFromIdAndComponents();
					if (tag == null) { return ItemStack.EMPTY; }
					return ItemStack.parse(registryAccess, tag).orElse(ItemStack.EMPTY);
			}
			return null;
		}

		private @Nullable CompoundTag tagFromIdAndComponents()
		{
			CompoundTag tag = new CompoundTag();

			try { tag.put("components", Utils.nbtFromString(data)); }
			catch (CommandSyntaxException e) { return null; }

			tag.put("id", StringTag.valueOf(BuiltInRegistries.ITEM.getKey(item).toString()));
			tag.put("count", IntTag.valueOf(1));
			return tag;
		}
	}
}
