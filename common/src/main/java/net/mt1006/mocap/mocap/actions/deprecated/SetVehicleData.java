package net.mt1006.mocap.mocap.actions.deprecated;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.mixin.fields.AbstractHorseFields;
import net.mt1006.mocap.mixin.fields.BoatFields;
import net.mt1006.mocap.mixin.fields.HorseFields;
import net.mt1006.mocap.mixin.fields.LlamaFields;
import net.mt1006.mocap.utils.EntityData;

public class SetVehicleData implements MocapAction
{
	private final boolean used;
	private final byte flags;      // AbstractHorse
	private final boolean flag1;   // Camel - is dashing; AbstractChestedHorse - has chest; Boat - is left paddle turning
	private final boolean flag2;   // AgeableMob - is baby; Boat - is right paddle turning
	private final int int1;        // Horse/Llama - variant; Boat - time since last hit; AbstractMinecart - shaking power
	private final int int2;        // Boat - hit direction; AbstractMinecart - shaking direction
	private final int int3;        // Boat - splash timer; AbstractMinecart - shaking multiplier
	private final float float1;    // Boat - damage taken

	public SetVehicleData(Reader reader)
	{
		used = reader.readBoolean();
		flags = used ? reader.readByte() : 0;
		flag1 = used && reader.readBoolean();
		flag2 = used && reader.readBoolean();
		int1 = used ? reader.readInt() : 0;
		int2 = used ? reader.readInt() : 0;
		int3 = used ? reader.readInt() : 0;
		float1 = used ? reader.readFloat() : 0;
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		//TODO: [CONVERTER] replace with exception
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
			else if (entity instanceof AbstractChestedHorse chestedHorse) { chestedHorse.setChest(flag1); }
			else if (entity instanceof Camel camel) { camel.setDashing(flag1); }

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
