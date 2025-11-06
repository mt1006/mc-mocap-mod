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
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapStateAction;
import net.mt1006.mocap.mixin.fields.AbstractHorseFields;
import net.mt1006.mocap.mixin.fields.BoatFields;
import net.mt1006.mocap.mixin.fields.HorseFields;
import net.mt1006.mocap.mixin.fields.LlamaFields;
import net.mt1006.mocap.utils.EntityData;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SetNonPlayerEntityData implements MocapStateAction
{
	private static final byte VAL_FLAG1 =  0b00000001;
	private static final byte VAL_FLAG2 =  0b00000010;
	private static final byte HAS_BYTE1 =  0b00000100;
	private static final byte HAS_INT1 =   0b00001000;
	private static final byte HAS_INT2 =   0b00010000;
	private static final byte HAS_INT3 =   0b00100000;
	private static final byte HAS_FLOAT1 = 0b01000000;

	private final boolean flag1;             // Camel - is dashing; AbstractChestedHorse - has chest; Boat - is left paddle turning
	private final boolean flag2;             // AgeableMob - is baby; Boat - is right paddle turning
	private final @Nullable Byte byte1;      // AbstractHorse
	private final @Nullable Integer int1;    // Horse/Llama - variant; Boat - time since last hit; AbstractMinecart - shaking power
	private final @Nullable Integer int2;    // Boat - hit direction; AbstractMinecart - shaking direction
	private final @Nullable Integer int3;    // Boat - splash timer; AbstractMinecart - shaking multiplier
	private final @Nullable Float float1;    // Boat - damage taken

	public static @Nullable SetNonPlayerEntityData fromEntity(Entity entity)
	{
		if (entity instanceof Player) { return null; }

		if (entity instanceof AbstractHorse)
		{
			boolean flag1 = false;
			Byte byte1 = EntityData.ABSTRACT_HORSE_FLAGS.valOrDef(entity, (byte)0);
			Integer int1 = null;

			if (entity instanceof Horse) { int1 = ((HorseFields)entity).callGetTypeVariant(); }
			else if (entity instanceof AbstractChestedHorse chestedHorse) { flag1 = chestedHorse.hasChest(); }
			else if (entity instanceof Camel camel) { flag1 = camel.isDashing(); }

			if (entity instanceof Llama llama) { int1 = llama.getVariant().getId(); }

			return new SetNonPlayerEntityData(flag1, false, byte1, int1, null, null, null);
		}
		else if (entity instanceof Boat boat)
		{
			return new SetNonPlayerEntityData(
					boat.getPaddleState(0),
					boat.getPaddleState(1),
					null,
					boat.getHurtTime(),
					boat.getHurtDir(),
					((BoatFields)entity).callGetBubbleTime(),
					boat.getDamage());
		}
		else if (entity instanceof AbstractMinecart minecart)
		{
			return new SetNonPlayerEntityData(
					false, false, null,
					minecart.getHurtTime(),
					minecart.getHurtDir(),
					null,
					minecart.getDamage());
		}
		else
		{
			return null;
		}
	}

	private SetNonPlayerEntityData(boolean flag1, boolean flag2, @Nullable Byte byte1, @Nullable Integer int1,
								   @Nullable Integer int2, @Nullable Integer int3, @Nullable Float float1)
	{
		this.byte1 = byte1;
		this.flag1 = flag1;
		this.flag2 = flag2;
		this.int1 = int1;
		this.int2 = int2;
		this.int3 = int3;
		this.float1 = float1;
	}

	public SetNonPlayerEntityData(MocapAction.Reader reader)
	{
		byte flags = reader.readByte();
		flag1 = (flags & VAL_FLAG1) != 0;
		flag2 = (flags & VAL_FLAG2) != 0;
		byte1 = (flags & HAS_BYTE1) != 0 ? reader.readByte() : null;
		int1 = (flags & HAS_INT1) != 0 ? reader.readPackedInt() : null;
		int2 = (flags & HAS_INT2) != 0 ? reader.readPackedInt() : null;
		int3 = (flags & HAS_INT3) != 0 ? reader.readPackedInt() : null;
		float1 = (flags & HAS_FLOAT1) != 0 ? reader.readFloat() : null;
	}

	@Override public boolean differs(MocapStateAction previousAction)
	{
		SetNonPlayerEntityData data = (SetNonPlayerEntityData)previousAction;

		return flag1 != data.flag1
				|| flag2 != data.flag2
				|| !Objects.equals(byte1, data.byte1)
				|| !Objects.equals(int1, data.int1)
				|| !Objects.equals(int2, data.int2)
				|| !Objects.equals(int3, data.int3)
				|| !Objects.equals(float1, data.float1);
	}

	@Override public void write(MocapAction.Writer writer, MocapRecordingData data)
	{
		byte flags = 0;
		flags |= flag1 ? VAL_FLAG1 : 0;
		flags |= flag2 ? VAL_FLAG2 : 0;
		flags |= byte1 != null ? HAS_BYTE1 : 0;
		flags |= int1 != null ? HAS_INT1 : 0;
		flags |= int2 != null ? HAS_INT2 : 0;
		flags |= int3 != null ? HAS_INT3 : 0;
		flags |= float1 != null ? HAS_FLOAT1 : 0;

		writer.addByte(flags);
		if (byte1 != null) { writer.addByte(byte1); }
		if (int1 != null) { writer.addPackedInt(int1); }
		if (int2 != null) { writer.addPackedInt(int2); }
		if (int3 != null) { writer.addPackedInt(int3); }
		if (float1 != null) { writer.addFloat(float1); }
	}

	@Override public MocapAction.Result execute(MocapActionContext ctx)
	{
		Entity entity = ctx.getEntity();

		if (entity instanceof AgeableMob ageableMob)
		{
			ageableMob.setAge(flag2 ? -1 : 0);
		}

		if (entity instanceof AbstractHorse)
		{
			if (byte1 == null) { return Result.ERROR; }
			EntityData.ABSTRACT_HORSE_FLAGS.set(entity, byte1);

			try
			{
				ItemStack itemStack = new ItemStack((byte1 & 0x04) != 0 ? Items.SADDLE : Items.AIR);
				((AbstractHorseFields)entity).getInventory().setItem(0, itemStack);
			}
			catch (Exception ignore) {}

			if (entity instanceof Horse) { ((HorseFields)entity).callSetTypeVariant(int1 != null ? int1 : 0); }
			else if (entity instanceof AbstractChestedHorse chestedHorse) { chestedHorse.setChest(flag1); }
			else if (entity instanceof Camel camel) { camel.setDashing(flag1); }

			if (entity instanceof Llama) { ((LlamaFields)entity).callSetVariant(Llama.Variant.byId(int1 != null ? int1 : 0)); }
		}
		else if (entity instanceof Boat boat)
		{
			if (int1 == null || int2 == null || int3 == null || float1 == null) { return Result.ERROR; }
			boat.setPaddleState(flag1, flag2);
			boat.setHurtTime(int1);
			boat.setHurtDir(int2);
			((BoatFields)entity).callSetBubbleTime(int3);
			boat.setDamage(float1);
		}
		else if (entity instanceof AbstractMinecart minecart)
		{
			if (int1 == null || int2 == null || float1 == null) { return Result.ERROR; }
			minecart.setHurtTime(int1);
			minecart.setHurtDir(int2);
			minecart.setDamage(float1);
		}

		return MocapAction.Result.OK;
	}
}
