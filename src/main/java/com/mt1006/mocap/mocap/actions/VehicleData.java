package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mixin.fields.*;
import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import com.mt1006.mocap.utils.EntityData;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class VehicleData implements ComparableAction
{
	private final boolean used;
	private byte flags = 0;         // AbstractHorse
	private boolean flag1 = false;  // Camel - is dashing; AbstractChestedHorse - has chest; Pig - has saddle; Boat - is left paddle turning
	private boolean flag2 = false;  // AgeableMob - is baby; Boat - is right paddle turning
	private int int1 = 0;           // Horse/Llama - variant; Boat - time since last hit; AbstractMinecart - shaking power
	private int int2 = 0;           // Llama - carpet color; Boat - hit direction; AbstractMinecart - shaking direction
	private int int3 = 0;           // Boat - splash timer; AbstractMinecart - shaking multiplier
	private float float1 = 0.0f;    // Boat - damage taken

	public VehicleData(Entity entity)
	{
		if (entity instanceof Player)
		{
			used = false;
			return;
		}

		if (entity instanceof AgeableMob)
		{
			flag2 = ((AgeableMob)entity).getAge() < 0;
		}

		if (entity instanceof AbstractHorse)
		{
			AbstractHorse abstractHorse = (AbstractHorse)entity;
			if (abstractHorse.isTamed()) { flags |= 0x02; }
			if (abstractHorse.isSaddled()) { flags |= 0x04; }
			if (abstractHorse.isBred()) { flags |= 0x08; }
			if (abstractHorse.isStanding()) { flags |= 0x20; }
			if ((EntityData.ABSTRACT_HORSE_FLAGS.valOrDef(entity, (byte)0) & 0x40) != 0) { flags |= 0x40; }

			if (entity instanceof Camel) { flag1 = ((Camel)entity).isDashing(); }
			else if (entity instanceof AbstractChestedHorse) { flag1 = ((AbstractChestedHorse)entity).hasChest(); }
			else if (entity instanceof Horse) { int1 = ((HorseMixin)entity).callGetTypeVariant(); }

			if (entity instanceof Llama)
			{
				DyeColor carpetColor = ((Llama)entity).getSwag();
				int1 = ((Llama)entity).getVariant().getId();
				int2 = carpetColor != null ? carpetColor.getId() : -1;
			}
		}
		else if (entity instanceof Pig)
		{
			flag1 = ((Pig) entity).isSaddled();
		}
		else if (entity instanceof Boat)
		{
			flag1 = ((Boat)entity).getPaddleState(0);
			flag2 = ((Boat)entity).getPaddleState(1);
			int1 = ((Boat)entity).getHurtTime();
			int2 = ((Boat)entity).getHurtDir();
			int3 = ((BoatMixin)entity).callGetBubbleTime();
			float1 = ((Boat)entity).getDamage();
		}
		else if (entity instanceof AbstractMinecart)
		{
			int1 = ((AbstractMinecart)entity).getHurtTime();
			int2 = ((AbstractMinecart)entity).getHurtDir();
			float1 = ((AbstractMinecart)entity).getDamage();
		}

		used = true;
	}

	public VehicleData(RecordingFiles.Reader reader)
	{
		used = reader.readBoolean();
		if (used)
		{
			flags = reader.readByte();
			flag1 = reader.readBoolean();
			flag2 = reader.readBoolean();
			int1 = reader.readInt();
			int2 = reader.readInt();
			int3 = reader.readInt();
			float1 = reader.readFloat();
		}
	}

	@Override public boolean differs(ComparableAction action)
	{
		VehicleData vehicleData = (VehicleData)action;

		if (!used && !vehicleData.used) { return false; }
		if (used != vehicleData.used) { return true; }
		return flags != vehicleData.flags
				|| flag1 != vehicleData.flag1
				|| flag2 != vehicleData.flag2
				|| int1 != vehicleData.int1
				|| int2 != vehicleData.int2
				|| int3 != vehicleData.int3
				|| float1 != vehicleData.float1;
	}

	@Override public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action)
	{
		if (action != null && !differs(action)) { return; }

		writer.addByte(Type.VEHICLE_DATA.id);
		writer.addBoolean(used);
		if (used)
		{
			writer.addByte(flags);
			writer.addBoolean(flag1);
			writer.addBoolean(flag2);
			writer.addInt(int1);
			writer.addInt(int2);
			writer.addInt(int3);
			writer.addFloat(float1);
		}
	}

	@Override public Result execute(PlayingContext ctx)
	{
		if (!used) { return Result.OK; }

		if (ctx.entity instanceof AgeableMob)
		{
			((AgeableMob)ctx.entity).setAge(flag2 ? -1 : 0);
		}

		if (ctx.entity instanceof AbstractHorse)
		{
			EntityData.ABSTRACT_HORSE_FLAGS.set(ctx.entity, flags);

			try
			{
				ItemStack itemStack = new ItemStack((flags & 0x04) != 0 ? Items.SADDLE : Items.AIR);
				((AbstractHorseMixin)ctx.entity).getInventory().setItem(0, itemStack);
			}
			catch (Exception ignore) {}

			if (ctx.entity instanceof Camel) { ((Camel)ctx.entity).setDashing(flag1); }
			else if (ctx.entity instanceof AbstractChestedHorse) { ((AbstractChestedHorse)ctx.entity).setChest(flag1); }
			else if (ctx.entity instanceof Horse) { ((HorseMixin)ctx.entity).callSetTypeVariant(int1); }

			if (ctx.entity instanceof Llama)
			{
				((Llama)ctx.entity).setVariant(Llama.Variant.byId(int1));
				((LlamaMixin)ctx.entity).callSetSwag(int2 != -1 ? DyeColor.byId(int2) : null);
			}
		}
		else if (ctx.entity instanceof Pig)
		{
			((PigMixin)ctx.entity).getSteering().setSaddle(flag1);
		}
		else if (ctx.entity instanceof Boat)
		{
			((Boat)ctx.entity).setPaddleState(flag1, flag2);
			((Boat)ctx.entity).setHurtTime(int1);
			((Boat)ctx.entity).setHurtDir(int2);
			((BoatMixin)ctx.entity).callSetBubbleTime(int3);
			((Boat)ctx.entity).setDamage(float1);
		}
		else if (ctx.entity instanceof AbstractMinecart)
		{
			((AbstractMinecart)ctx.entity).setHurtTime(int1);
			((AbstractMinecart)ctx.entity).setHurtDir(int2);
			((AbstractMinecart)ctx.entity).setDamage(float1);
		}

		return Result.OK;
	}
}
