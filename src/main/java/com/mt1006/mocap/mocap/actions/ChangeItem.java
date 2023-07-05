package com.mt1006.mocap.mocap.actions;

import com.mojang.datafixers.util.Pair;
import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ChangeItem implements ComparableAction
{
	private static final byte NO_ITEM = 0;
	private static final byte ID_ONLY = 1;
	private static final byte ID_AND_NBT = 2;
	private static final int ITEM_COUNT = 6;
	private final List<Pair<Integer, String>> items = new ArrayList<>();

	public ChangeItem(Entity entity)
	{
		if (!(entity instanceof LivingEntity)) { return; }
		LivingEntity livingEntity = (LivingEntity)entity;

		addItem(livingEntity.getMainHandItem());
		addItem(livingEntity.getOffhandItem());
		livingEntity.getArmorSlots().forEach(this::addItem);
	}

	public ChangeItem(RecordingFiles.Reader reader)
	{
		for (int i = 0; i < ITEM_COUNT; i++)
		{
			byte type = reader.readByte();

			if (type == ID_AND_NBT) { items.add(new Pair<>(reader.readInt(), reader.readString())); }
			else if (type == ID_ONLY) { items.add(new Pair<>(reader.readInt(), null)); }
			else { items.add(null); }
		}
	}

	private void addItem(@Nullable ItemStack itemStack)
	{
		if (itemStack == null)
		{
			items.add(null);
			return;
		}

		String nbtString;
		if (itemStack.getTag() == null) { nbtString = null; }
		else { nbtString = itemStack.getTag().getAsString(); }

		items.add(new Pair<>(Item.getId(itemStack.getItem()), nbtString));
	}

	@Override public boolean differs(ComparableAction action)
	{
		if (items.size() != ((ChangeItem)action).items.size()) { return true; }

		for (int i = 0; i < items.size(); i++)
		{
			Pair<Integer, String> item1 = items.get(i);
			Pair<Integer, String> item2 = ((ChangeItem)action).items.get(i);

			if (item1 == null && item2 == null) { continue; }
			if ((item1 == null) != (item2 == null)) { return true; }
			if (item1.getFirst().intValue() != item2.getFirst().intValue()) { return true; }

			if (item1.getSecond() == null && item2.getSecond() == null) { continue; }
			if ((item1.getSecond() == null) != (item2.getSecond() == null)) { return true; }
			if (!item1.getSecond().equals(item2.getSecond())) { return true; }
		}
		return false;
	}

	@Override public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action)
	{
		if (action != null && !differs(action)) { return; }

		int count = 0;
		for (Pair<Integer, String> ignored : items) { count++; }
		if (count != ITEM_COUNT) { return; }

		writer.addByte(Type.CHANGE_ITEM.id);

		for (Pair<Integer, String> item : items)
		{
			if (item == null)
			{
				writer.addByte(NO_ITEM);
			}
			else if (item.getSecond() == null)
			{
				writer.addByte(ID_ONLY);
				writer.addInt(item.getFirst());
			}
			else
			{
				writer.addByte(ID_AND_NBT);
				writer.addInt(item.getFirst());
				writer.addString(item.getSecond());
			}
		}
	}

	@Override public Result execute(PlayingContext ctx)
	{
		List<Pair<EquipmentSlot, ItemStack>> list = new ArrayList<>();

		if (items.size() != ITEM_COUNT) { return Result.ERROR; }
		for (int i = 0; i < ITEM_COUNT; i++)
		{
			Pair<Integer, String> item = items.get(i);
			if (item == null) { continue; }

			ItemStack itemStack;
			if (item.getSecond() != null)
			{
				try
				{
					CompoundTag compoundTag = Utils.nbtFromString(item.getSecond());
					itemStack = new ItemStack(Item.byId(item.getFirst()));
					itemStack.setTag(compoundTag);
				}
				catch (Exception exception)
				{
					return Result.ERROR;
				}
			}
			else
			{
				itemStack = new ItemStack(Item.byId(item.getFirst()));
			}

			switch (i)
			{
				case 0: list.add(new Pair<>(EquipmentSlot.MAINHAND, itemStack)); break;
				case 1: list.add(new Pair<>(EquipmentSlot.OFFHAND, itemStack)); break;
				case 2: list.add(new Pair<>(EquipmentSlot.FEET, itemStack)); break;
				case 3: list.add(new Pair<>(EquipmentSlot.LEGS, itemStack)); break;
				case 4: list.add(new Pair<>(EquipmentSlot.CHEST, itemStack)); break;
				case 5: list.add(new Pair<>(EquipmentSlot.HEAD, itemStack)); break;
			}
		}

		ctx.packetTargets.broadcastAll(new ClientboundSetEquipmentPacket(ctx.entity.getId(), list));
		return Result.OK;
	}
}
