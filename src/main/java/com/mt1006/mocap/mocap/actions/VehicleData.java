package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mixin.fields.BoatMixin;
import com.mt1006.mocap.mixin.fields.HorseMixin;
import com.mt1006.mocap.mixin.fields.LlamaMixin;
import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import com.mt1006.mocap.utils.EntityData;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.horse.AbstractChestedHorseEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
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
		if (entity instanceof PlayerEntity)
		{
			used = false;
			return;
		}

		if (entity instanceof AgeableEntity)
		{
			flag2 = ((AgeableEntity)entity).getAge() < 0;
		}

		if (entity instanceof AbstractHorseEntity)
		{
			AbstractHorseEntity abstractHorse = (AbstractHorseEntity)entity;
			if (abstractHorse.isTamed()) { flags |= 0x02; }
			if (abstractHorse.isSaddled()) { flags |= 0x04; }
			if (abstractHorse.isBred()) { flags |= 0x08; }
			if (abstractHorse.isStanding()) { flags |= 0x20; }
			if ((EntityData.ABSTRACT_HORSE_FLAGS.valOrDef(entity, (byte)0) & 0x40) != 0) { flags |= 0x40; }

			else if (entity instanceof AbstractChestedHorseEntity) { flag1 = ((AbstractChestedHorseEntity)entity).hasChest(); }
			else if (entity instanceof HorseEntity) { int1 = ((HorseMixin)entity).callGetTypeVariant(); }

			if (entity instanceof LlamaEntity)
			{
				DyeColor carpetColor = ((LlamaEntity)entity).getSwag();
				int1 = ((LlamaEntity)entity).getVariant();
				int2 = carpetColor != null ? carpetColor.getId() : -1;
			}
		}
		else if (entity instanceof PigEntity)
		{
			flag1 = EntityData.PIG_HAS_SADDLE.valOrDef(entity, false);
		}
		else if (entity instanceof BoatEntity)
		{
			flag1 = ((BoatEntity)entity).getPaddleState(0);
			flag2 = ((BoatEntity)entity).getPaddleState(1);
			int1 = ((BoatEntity)entity).getHurtTime();
			int2 = ((BoatEntity)entity).getHurtDir();
			int3 = ((BoatMixin)entity).callGetBubbleTime();
			float1 = ((BoatEntity)entity).getDamage();
		}
		else if (entity instanceof AbstractMinecartEntity)
		{
			int1 = ((AbstractMinecartEntity)entity).getHurtTime();
			int2 = ((AbstractMinecartEntity)entity).getHurtDir();
			float1 = ((AbstractMinecartEntity)entity).getDamage();
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
		EntityData entityData = new EntityData(ctx.entity);

		if (ctx.entity instanceof AgeableEntity)
		{
			((AgeableEntity)ctx.entity).setAge(flag2 ? -1 : 0);
		}

		if (ctx.entity instanceof AbstractHorseEntity)
		{
			//TODO: replace EntityData
			entityData.add(EntityData.ABSTRACT_HORSE_FLAGS, flags);

			if (ctx.entity instanceof AbstractChestedHorseEntity) { ((AbstractChestedHorseEntity)ctx.entity).setChest(flag1); }
			else if (ctx.entity instanceof HorseEntity) { ((HorseMixin)ctx.entity).callSetTypeVariant(int1); }

			if (ctx.entity instanceof LlamaEntity)
			{
				((LlamaEntity)ctx.entity).setVariant(int1);
				((LlamaMixin)ctx.entity).callSetSwag(int2 != -1 ? DyeColor.byId(int2) : null);
			}
		}
		else if (ctx.entity instanceof PigEntity)
		{
			entityData.add(EntityData.PIG_HAS_SADDLE, flag1);
		}
		else if (ctx.entity instanceof BoatEntity)
		{
			((BoatEntity)ctx.entity).setPaddleState(flag1, flag2);
			((BoatEntity)ctx.entity).setHurtTime(int1);
			((BoatEntity)ctx.entity).setHurtDir(int2);
			((BoatMixin)ctx.entity).callSetBubbleTime(int3);
			((BoatEntity)ctx.entity).setDamage(float1);
		}
		else if (ctx.entity instanceof AbstractMinecartEntity)
		{
			((AbstractMinecartEntity)ctx.entity).setHurtTime(int1);
			((AbstractMinecartEntity)ctx.entity).setHurtDir(int2);
			((AbstractMinecartEntity)ctx.entity).setDamage(float1);
		}

		entityData.broadcast(ctx);
		return Result.OK;
	}
}
