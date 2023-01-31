package com.mt1006.mocap.mocap.actions;

import com.mojang.brigadier.StringReader;
import com.mojang.datafixers.util.Pair;
import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.mocap.playing.PlayerActions;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ChangeItem implements Action
{
	private static final byte NO_ITEM = 0;
	private static final byte ID_ONLY = 1;
	private static final byte ID_AND_NBT = 2;
	private static final int ITEM_COUNT = 6;
	private final List<Pair<Integer, String>> items = new ArrayList<>();

	public ChangeItem(Player player)
	{
		addItem(player.getMainHandItem());
		addItem(player.getOffhandItem());

		for (ItemStack itemStack : player.getArmorSlots())
		{
			addItem(itemStack);
		}
	}

	public ChangeItem(RecordingFile.Reader reader)
	{
		for (int i = 0; i < ITEM_COUNT; i++)
		{
			byte type = reader.readByte();

			if (type == ID_AND_NBT) { items.add(new Pair<>(reader.readInt(), reader.readString())); }
			else if (type == ID_ONLY) { items.add(new Pair<>(reader.readInt(), null)); }
			else { items.add(null); }
		}
	}

	public void write(RecordingFile.Writer writer, @Nullable PlayerActions actions)
	{
		if (!differs(actions)) { return; }
		writer.addByte(CHANGE_ITEM);

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

	public boolean differs(@Nullable PlayerActions actions)
	{
		if (actions == null) { return true; }

		if (items.size() != actions.changeItem.items.size()) { return true; }

		for (int i = 0; i < items.size(); i++)
		{
			Pair<Integer, String> item1 = items.get(i);
			Pair<Integer, String> item2 = actions.changeItem.items.get(i);

			if (item1 == null && item2 == null) { continue; }
			if ((item1 == null) != (item2 == null)) { return true; }
			if (item1.getFirst().intValue() != item2.getFirst().intValue()) { return true; }

			if (item1.getSecond() == null && item2.getSecond() == null) { continue; }
			if ((item1.getSecond() == null) != (item2.getSecond() == null)) { return true; }
			if (!item1.getSecond().equals(item2.getSecond())) { return true; }
		}
		return false;
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

	@Override public int execute(PlayerList packetTargets, FakePlayer fakePlayer, Vec3i blockOffset)
	{
		List<Pair<EquipmentSlot, ItemStack>> list = new ArrayList<>();

		if (items.size() != ITEM_COUNT) { return RET_ERROR; }
		for (int i = 0; i < ITEM_COUNT; i++)
		{
			Pair<Integer, String> item = items.get(i);
			if (item == null) { continue; }

			ItemStack itemStack;
			if (item.getSecond() != null)
			{
				try
				{
					CompoundTag compoundTag = new TagParser(new StringReader(item.getSecond())).readStruct();
					itemStack = new ItemStack(Item.byId(item.getFirst()));
					itemStack.setTag(compoundTag);
				}
				catch (Exception exception)
				{
					return RET_ERROR;
				}
			}
			else
			{
				itemStack = new ItemStack(Item.byId(item.getFirst()));
			}

			switch (i)
			{
				case 0 -> list.add(new Pair<>(EquipmentSlot.MAINHAND, itemStack));
				case 1 -> list.add(new Pair<>(EquipmentSlot.OFFHAND, itemStack));
				case 2 -> list.add(new Pair<>(EquipmentSlot.FEET, itemStack));
				case 3 -> list.add(new Pair<>(EquipmentSlot.LEGS, itemStack));
				case 4 -> list.add(new Pair<>(EquipmentSlot.CHEST, itemStack));
				case 5 -> list.add(new Pair<>(EquipmentSlot.HEAD, itemStack));
			}
		}

		packetTargets.broadcastAll(new ClientboundSetEquipmentPacket(fakePlayer.getId(), list));
		return RET_OK;
	}
}
