package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.Playing;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import com.mt1006.mocap.mocap.settings.Settings;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class EntityUpdate implements Action
{
	public static final byte ADD = 1;
	public static final byte REMOVE = 2;
	public static final byte KILL = 3;
	public static final byte HURT = 4;
	public static final byte PLAYER_MOUNT = 5;
	public static final byte PLAYER_DISMOUNT = 6;
	private final byte type;
	private final int id;
	private final @Nullable String nbtString;
	private final double[] position = new double[3];

	public EntityUpdate(byte type)
	{
		this.type = type;
		this.id = 0;
		this.nbtString = null;
	}

	public EntityUpdate(byte type, int id)
	{
		this.type = type;
		this.id = id;
		this.nbtString = null;
	}

	public EntityUpdate(byte type, int id, Entity entity)
	{
		this.type = type;
		this.id = id;

		nbtString = serializeEntityNBT(entity).toString();

		Vec3 entityPos = entity.position();
		this.position[0] = entityPos.x;
		this.position[1] = entityPos.y;
		this.position[2] = entityPos.z;
	}

	public EntityUpdate(RecordingFiles.Reader reader)
	{
		type = reader.readByte();
		id = reader.readInt();

		if (type == ADD)
		{
			nbtString = reader.readString();
			position[0] = reader.readDouble();
			position[1] = reader.readDouble();
			position[2] = reader.readDouble();
		}
		else
		{
			nbtString = null;
		}
	}

	public static CompoundTag serializeEntityNBT(Entity entity)
	{
		CompoundTag compoundTag = new CompoundTag();

		String id = entity.getEncodeId();
		compoundTag.putString("id", id != null ? id : "minecraft:cow");

		entity.saveWithoutId(compoundTag);
		compoundTag.remove("UUID");
		compoundTag.remove("Pos");
		compoundTag.remove("Motion");
		return compoundTag;
	}

	public static boolean isEntityPlayingDisabled()
	{
		return !(Settings.PLAY_BLOCK_ACTIONS.val || Settings.PLAY_ITEM_ENTITIES.val || Settings.PLAY_OTHER_ENTITIES.val);
	}

	public void write(RecordingFiles.Writer writer)
	{
		writer.addByte(Type.ENTITY_UPDATE.id);

		writer.addByte(type);
		writer.addInt(id);

		if (type == ADD)
		{
			writer.addString(nbtString != null ? nbtString : "");
			writer.addDouble(position[0]);
			writer.addDouble(position[1]);
			writer.addDouble(position[2]);
		}
	}

	@Override public Result execute(PlayingContext ctx)
	{
		if (type == ADD)
		{
			if (nbtString == null || ctx.entityMap.containsKey(id) || isEntityPlayingDisabled()) { return Result.IGNORED; }

			CompoundTag nbt;
 			try { nbt = Utils.nbtFromString(nbtString); }
			catch (Exception exception) { return Result.ERROR; }

			Entity entity = EntityType.create(nbt, ctx.level).orElse(null);
			if (entity == null) { return Result.IGNORED; }

			if (entity instanceof Saddleable || entity instanceof Minecart || entity instanceof Boat)
			{
				if (!Settings.PLAY_VEHICLE_ENTITIES.val) { return Result.IGNORED; }
			}
			else if (entity instanceof ItemEntity)
			{
				if (!Settings.PLAY_ITEM_ENTITIES.val) { return Result.IGNORED; }
			}
			else if (!Settings.PLAY_OTHER_ENTITIES.val)
			{
				return Result.IGNORED;
			}

			entity.setPos(position[0] + ctx.offset.x, position[1] + ctx.offset.y, position[2] + ctx.offset.z);
			entity.setDeltaMovement(0.0, 0.0, 0.0);
			entity.setNoGravity(true);
			entity.setInvulnerable(true);
			entity.addTag(Playing.MOCAP_ENTITY_TAG);
			if (entity instanceof Mob) { ((Mob)entity).setNoAi(true); }

			ctx.level.addFreshEntity(entity);
			ctx.entityMap.put(id, entity);
			return Result.OK;
		}
		else if (type == PLAYER_DISMOUNT)
		{
			ctx.entity.stopRiding();
			return Result.OK;
		}
		else
		{
			Entity entity = ctx.entityMap.get(id);
			if (entity == null) { return Result.IGNORED; }

			if (type == REMOVE)
			{
				entity.remove(Entity.RemovalReason.KILLED);
				return Result.OK;
			}
			else if (type == KILL)
			{
				entity.invulnerableTime = 0; // for sound effect
				entity.kill();
				return Result.OK;
			}
			else if (type == HURT)
			{
				Hurt.hurtEntity(entity);
				return Result.OK;
			}
			else if (type == PLAYER_MOUNT)
			{
				ctx.entity.startRiding(entity, true);
				return Result.OK;
			}
			else
			{
				return Result.IGNORED;
			}
		}
	}
}
