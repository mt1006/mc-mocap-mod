package net.mt1006.mocap.mocap.actions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
	private static final int ITEM_COUNT = 8;
	private final byte itemCount;
	private final List<ItemData> items = new ArrayList<>();

	public static @Nullable ChangeItem fromEntity(Entity entity)
	{
		return (entity instanceof LivingEntity livingEntity) ? new ChangeItem(livingEntity) : null;
	}

	private ChangeItem(LivingEntity entity)
	{
		DynamicOps<Tag> ops = entity.registryAccess().createSerializationContext(NbtOps.INSTANCE);

		addItem(entity.getMainHandItem(), ops);
		addItem(entity.getOffhandItem(), ops);
		addItem(entity.getItemBySlot(EquipmentSlot.FEET), ops);
		addItem(entity.getItemBySlot(EquipmentSlot.LEGS), ops);
		addItem(entity.getItemBySlot(EquipmentSlot.CHEST), ops);
		addItem(entity.getItemBySlot(EquipmentSlot.HEAD), ops);
		addItem(entity.getItemBySlot(EquipmentSlot.BODY), ops);
		addItem(entity.getItemBySlot(EquipmentSlot.SADDLE), ops);

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
			// backwards compatibility
			reader.shift(-1);
			itemCount = ITEM_COUNT_LEGACY;
		}
		else
		{
			itemCount = (byte)(firstByte != Byte.MIN_VALUE ? -firstByte : 0);
		}

		for (int i = 0; i < itemCount; i++) { items.add(new ItemData(reader, data)); }
	}

	private void addItem(@Nullable ItemStack itemStack, DynamicOps<Tag> ops)
	{
		items.add(ItemData.get(itemStack, ops));
	}

	private void setEntityItems(LivingEntity entity)
	{
		DynamicOps<Tag> ops = entity.registryAccess().createSerializationContext(NbtOps.INSTANCE);
		for (int i = 0; i < ITEM_COUNT; i++)
		{
			ItemData item = i < itemCount ? items.get(i) : ItemData.EMPTY;
			ItemStack itemStack = item.getItemStack(ops);

			switch (i)
			{
				case 0 -> entity.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
				case 1 -> entity.setItemSlot(EquipmentSlot.OFFHAND, itemStack);
				case 2 -> entity.setItemSlot(EquipmentSlot.FEET, itemStack);
				case 3 -> entity.setItemSlot(EquipmentSlot.LEGS, itemStack);
				case 4 -> entity.setItemSlot(EquipmentSlot.CHEST, itemStack);
				case 5 -> entity.setItemSlot(EquipmentSlot.HEAD, itemStack);
				case 6 -> entity.setItemSlot(EquipmentSlot.BODY, itemStack);
				case 7 -> entity.setItemSlot(EquipmentSlot.SADDLE, itemStack);
			}

			// for non-player living entities it's detected in their "tick" method
			if (entity instanceof Player) { ((LivingEntityFields)entity).callDetectEquipmentUpdates(); }
		}
	}

	@Override public boolean differs(MocapStateAction previousAction)
	{
		if (itemCount != ((ChangeItem)previousAction).itemCount) { return true; }

		for (int i = 0; i < itemCount; i++)
		{
			ItemData item1 = items.get(i);
			ItemData item2 = ((ChangeItem)previousAction).items.get(i);
			if (item1.differs(item2)) { return true; }
		}
		return false;
	}

	@Override public boolean shouldBeInitialized()
	{
		return itemCount != 0;
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

		private ItemData(ItemStack itemStack, DynamicOps<Tag> ops)
		{
			item = itemStack.getItem();
			Tag tag;
			try { tag = ItemStack.CODEC.encodeStart(ops, itemStack).getOrThrow(); }
			catch (Exception exception) { tag = null; }

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

		public static ItemData get(@Nullable ItemStack itemStack, DynamicOps<Tag> ops)
		{
			return (itemStack == null || itemStack.isEmpty()) ? EMPTY : new ItemData(itemStack, ops);
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

		public ItemStack getItemStack(DynamicOps<Tag> ops)
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
					try { return ItemStack.CODEC.parse(ops, tag).getOrThrow(); }
					catch (Exception e) { return ItemStack.EMPTY; }
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
