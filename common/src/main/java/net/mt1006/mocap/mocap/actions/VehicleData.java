package net.mt1006.mocap.mocap.actions;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapStateAction;
import net.mt1006.mocap.mixin.fields.AbstractHorseFields;
import net.mt1006.mocap.mixin.fields.BoatFields;
import net.mt1006.mocap.mixin.fields.HorseFields;
import net.mt1006.mocap.mixin.fields.LlamaFields;
import net.mt1006.mocap.utils.EntityData;

public class VehicleData implements MocapStateAction
{
	private final boolean used;
	private byte flags = 0;         // AbstractHorse
	private boolean flag1 = false;  // Camel - is dashing; AbstractChestedHorse - has chest; Boat - is left paddle turning
	private boolean flag2 = false;  // AgeableMob - is baby (deprecated); Boat - is right paddle turning
	private int int1 = 0;           // Horse/Llama - variant; Boat - time since last hit; AbstractMinecart - shaking power
	private int int2 = 0;           // Boat - hit direction; AbstractMinecart - shaking direction
	private int int3 = 0;           // Boat - splash timer; AbstractMinecart - shaking multiplier
	private float float1 = 0.0f;    // Boat - damage taken

	public VehicleData(Entity entity)
	{
		if (entity instanceof Player)
		{
			used = false;
			return;
		}

		if (entity instanceof AbstractHorse abstractHorse)
		{
			if (abstractHorse.isTamed()) { flags |= 0x02; }
			if (abstractHorse.isSaddled()) { flags |= 0x04; }
			if (abstractHorse.isBred()) { flags |= 0x08; }
			if (abstractHorse.isStanding()) { flags |= 0x20; }
			if ((EntityData.ABSTRACT_HORSE_FLAGS.valOrDef(entity, (byte)0) & 0x40) != 0) { flags |= 0x40; }

			if (entity instanceof Horse) { int1 = ((HorseFields)entity).callGetTypeVariant(); }
			else if (entity instanceof AbstractChestedHorse) { flag1 = ((AbstractChestedHorse)entity).hasChest(); }
			else if (entity instanceof Camel) { flag1 = ((Camel)entity).isDashing(); }

			if (entity instanceof Llama) { int1 = ((Llama)entity).getVariant().getId(); }
			used = true;
		}
		else if (entity instanceof Boat boat)
		{
			flag1 = boat.getPaddleState(0);
			flag2 = boat.getPaddleState(1);
			int1 = boat.getHurtTime();
			int2 = boat.getHurtDir();
			int3 = ((BoatFields)entity).callGetBubbleTime();
			float1 = boat.getDamage();
			used = true;
		}
		else if (entity instanceof AbstractMinecart minecart)
		{
			int1 = minecart.getHurtTime();
			int2 = minecart.getHurtDir();
			float1 = minecart.getDamage();
			used = true;
		}
		else
		{
			used = false;
		}
	}

	public VehicleData(Reader reader)
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

	@Override public boolean differs(MocapStateAction previousAction)
	{
		VehicleData vehicleData = (VehicleData)previousAction;

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

	@Override public boolean shouldBeInitialized()
	{
		return used;
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
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

	@Override public Result execute(MocapActionContext ctx)
	{
		if (!used) { return Result.IGNORED; }
		Entity entity = ctx.getEntity();

		if (entity instanceof AgeableMob ageableMob)
		{
			ageableMob.setAge(flag2 ? -1 : 0);
		}

		if (entity instanceof AbstractHorse)
		{
			EntityData.ABSTRACT_HORSE_FLAGS.set(entity, flags);

			try
			{
				ItemStack itemStack = new ItemStack((flags & 0x04) != 0 ? Items.SADDLE : Items.AIR);
				((AbstractHorseFields)entity).getInventory().setItem(0, itemStack);
			}
			catch (Exception ignore) {}

			if (entity instanceof Horse) { ((HorseFields)entity).callSetTypeVariant(int1); }
			else if (entity instanceof AbstractChestedHorse) { ((AbstractChestedHorse)entity).setChest(flag1); }
			else if (entity instanceof Camel) { ((Camel)entity).setDashing(flag1); }

			if (entity instanceof Llama) { ((LlamaFields)entity).callSetVariant(Llama.Variant.byId(int1)); }
		}
		else if (entity instanceof Boat boat)
		{
			boat.setPaddleState(flag1, flag2);
			boat.setHurtTime(int1);
			boat.setHurtDir(int2);
			((BoatFields)entity).callSetBubbleTime(int3);
			boat.setDamage(float1);
		}
		else if (entity instanceof AbstractMinecart minecart)
		{
			minecart.setHurtTime(int1);
			minecart.setHurtDir(int2);
			minecart.setDamage(float1);
		}

		return Result.OK;
	}
}
