package com.mt1006.mocap.mocap.actions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mt1006.mocap.mixin.fields.LivingEntityMixin;
import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import com.mt1006.mocap.utils.Utils;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ChangeItem implements ComparableAction
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
			return;
		}
		LivingEntity livingEntity = (LivingEntity)entity;

		addItem(livingEntity.getMainHandItem(), entity);
		addItem(livingEntity.getOffhandItem(), entity);
		livingEntity.getArmorSlots().forEach((item) -> addItem(item, entity));
		addItem(livingEntity.getItemBySlot(EquipmentSlot.BODY), entity);

		int itemCounter = 0;
		for (int i = 0; i < ITEM_COUNT; i++)
		{
			if (items.get(i).type != ItemDataType.NO_ITEM) { itemCounter = i + 1; }
		}
		itemCount = (byte)itemCounter;
	}

	public ChangeItem(RecordingFiles.Reader reader)
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
			for (int i = 0; i < ITEM_COUNT; i++) { items.add(new ItemData(reader)); }
			for (int i = ITEM_COUNT; i < itemCount; i++) { new ItemData(reader); }
		}
		else
		{
			for (int i = 0; i < itemCount; i++) { items.add(new ItemData(reader)); }
			for (int i = itemCount; i < ITEM_COUNT; i++) { items.add(ItemData.EMPTY); }
		}

	}

	private void addItem(@Nullable ItemStack itemStack, Entity entity)
	{
		items.add(ItemData.get(itemStack, entity.registryAccess()));
	}

	@Override public boolean differs(ComparableAction action)
	{
		if (items.size() != ((ChangeItem)action).items.size()) { return true; }

		for (int i = 0; i < items.size(); i++)
		{
			ItemData item1 = items.get(i);
			ItemData item2 =  ((ChangeItem)action).items.get(i);
			if (item1.differs(item2)) { return true; }
		}
		return false;
	}

	@Override public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action)
	{
		if (action != null && !differs(action)) { return; }
		if (itemCount > items.size()) { throw new RuntimeException(); }

		writer.addByte(Type.CHANGE_ITEM.id);
		writer.addByte(itemCount > 0 ? (byte)(-itemCount) : Byte.MIN_VALUE);

		for (int i = 0; i < itemCount; i++)
		{
			items.get(i).write(writer);
		}
	}

	@Override public Result execute(PlayingContext ctx)
	{
		if (items.size() != ITEM_COUNT) { return Result.ERROR; }
		if (!(ctx.entity instanceof LivingEntity)) { return Result.IGNORED; }
		LivingEntity entity = (LivingEntity)ctx.entity;

		for (int i = 0; i < ITEM_COUNT; i++)
		{
			ItemData item = items.get(i);
			ItemStack itemStack = item.getItemStack(ctx.entity.registryAccess());

			switch (i)
			{
				case 0: entity.setItemSlot(EquipmentSlot.MAINHAND, itemStack); break;
				case 1: entity.setItemSlot(EquipmentSlot.OFFHAND, itemStack); break;
				case 2: entity.setItemSlot(EquipmentSlot.FEET, itemStack); break;
				case 3: entity.setItemSlot(EquipmentSlot.LEGS, itemStack); break;
				case 4: entity.setItemSlot(EquipmentSlot.CHEST, itemStack); break;
				case 5: entity.setItemSlot(EquipmentSlot.HEAD, itemStack); break;
				case 6: entity.setItemSlot(EquipmentSlot.BODY, itemStack); break;
			}

			//TODO: do something with this
			/*if (i == 4 && entity instanceof AbstractHorse)
			{
				try { ((AbstractHorseMixin)entity).getInventory().setItem(1, itemStack); }
				catch (Exception ignore) {}
			}*/

			// for non-player living entities it's detected in their "tick" method
			if (entity instanceof Player) { ((LivingEntityMixin)ctx.entity).callDetectEquipmentUpdates();}
		}
		return Result.OK;
	}

	private enum ItemDataType
	{
		NO_ITEM(0, false, false),
		ID_ONLY(1, true, false),
		ID_AND_NBT(2, true, true),
		ID_AND_COMPONENTS(3, true, true);

		public final byte id;
		public final boolean hasId;
		public final boolean hasData;

		ItemDataType(int id, boolean hasId, boolean hasData)
		{
			this.id = (byte)id;
			this.hasId = hasId;
			this.hasData = hasData;
		}

		//TODO: optimize it?
		public static ItemDataType get(byte id)
		{
			for (ItemDataType type : values())
			{
				if (type.id == id) { return type; }
			}
			return NO_ITEM;
		}
	}

	private static class ItemData
	{
		public static final ItemData EMPTY = new ItemData();
		public final ItemDataType type;
		public final int itemId;
		public final String data;

		private ItemData()
		{
			type = ItemDataType.NO_ITEM;
			itemId = 0;
			data = "";
		}

		private ItemData(ItemStack itemStack, RegistryAccess registryAccess)
		{
			itemId = Item.getId(itemStack.getItem());
			Tag tag = itemStack.save(registryAccess);

			if (!(tag instanceof CompoundTag) || !((CompoundTag)tag).contains("components", Tag.TAG_COMPOUND))
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

		public ItemData(RecordingFiles.Reader reader)
		{
			type = ItemDataType.get(reader.readByte());
			itemId = type.hasId ? reader.readInt() : 0;
			data = type.hasData ? reader.readString() : "";
		}

		public static ItemData get(@Nullable ItemStack itemStack, RegistryAccess registryAccess)
		{
			return (itemStack == null || itemStack.isEmpty()) ? EMPTY : new ItemData(itemStack, registryAccess);
		}

		public boolean differs(ItemData itemData)
		{
			return type != itemData.type || itemId != itemData.itemId || !data.equals(itemData.data);
		}

		public void write(RecordingFiles.Writer writer)
		{
			writer.addByte(type.id);
			if (type.hasId) { writer.addInt(itemId); }
			if (type.hasData) { writer.addString(data); }
		}

		public ItemStack getItemStack(RegistryAccess registryAccess)
		{
			switch (type)
			{
				case NO_ITEM:
					return ItemStack.EMPTY;

				case ID_ONLY:
				case ID_AND_NBT: //TODO: convert old nbts?
					return new ItemStack(Item.byId(itemId));

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
			catch (CommandSyntaxException exception) { return null; }

			tag.put("id", StringTag.valueOf(BuiltInRegistries.ITEM.getKey(Item.byId(itemId)).toString()));
			tag.put("count", IntTag.valueOf(1));
			return tag;
		}
	}
}
