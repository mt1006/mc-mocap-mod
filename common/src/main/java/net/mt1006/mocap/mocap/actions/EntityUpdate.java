package net.mt1006.mocap.mocap.actions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.modifiers.MocapEntityFilter;
import net.mt1006.mocap.command.converter.AlphaConverter;
import net.mt1006.mocap.mixin.fields.EntityIdFields;
import net.mt1006.mocap.mocap.playing.PlaybackManager;
import net.mt1006.mocap.utils.Utils;
import org.jetbrains.annotations.Nullable;

public class EntityUpdate implements MocapAction
{
	private final UpdateType type;
	private final int id;
	private final @Nullable String nbtString;
	private final @Nullable Vec3 position;

	public static EntityUpdate addEntity(int id, Entity entity)
	{
		String nbtString = serializeEntityNBT(entity).toString();
		return new EntityUpdate(UpdateType.ADD, id, nbtString, entity.position());
	}

	public static EntityUpdate removeEntity(int id)
	{
		return new EntityUpdate(UpdateType.REMOVE, id, null, null);
	}

	public static EntityUpdate kill(int id)
	{
		return new EntityUpdate(UpdateType.KILL, id, null, null);
	}

	public static EntityUpdate hurt(int id)
	{
		return new EntityUpdate(UpdateType.HURT, id, null, null);
	}

	public static EntityUpdate playerMount(int id)
	{
		return new EntityUpdate(UpdateType.PLAYER_MOUNT, id, null, null);
	}

	public static EntityUpdate playerDismount()
	{
		return new EntityUpdate(UpdateType.PLAYER_DISMOUNT, 0, null, null);
	}

	private EntityUpdate(UpdateType type, int id, @Nullable String nbtString, @Nullable Vec3 position)
	{
		this.type = type;
		this.id = id;
		this.nbtString = nbtString;
		this.position = position;
	}

	//TODO: [CONVERTER] remove
	public EntityUpdate(Reader reader)
	{
		this(reader, null, 0);
	}

	//TODO: [CONVERTER] remove two last args
	public EntityUpdate(Reader reader, @Nullable AlphaConverter converter, int dummy)
	{
		type = UpdateType.fromId(reader.readByte());
		id = reader.readInt();

		if (type == UpdateType.ADD)
		{
			nbtString = reader.readString();
			position = reader.readVec3();

			//TODO: [CONVERTER] remove
			if (converter != null)
			{
				converter.posByEntity.put(id, new double[]{position.x, position.y, position.z});
			}
		}
		else
		{
			nbtString = null;
			position = null;
		}
	}

	public static CompoundTag serializeEntityNBT(Entity entity)
	{
		TagValueOutput nbt = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());

		String id = ((EntityIdFields)entity).callGetEncodeId();
		nbt.putString("id", id != null ? id : "minecraft:cow");
		entity.saveWithoutId(nbt);

		CompoundTag compoundTag = nbt.buildResult();
		compoundTag.remove("UUID");
		compoundTag.remove("Pos");
		compoundTag.remove("Motion");
		return compoundTag;
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addByte(type.id);
		writer.addInt(id);

		if (type == UpdateType.ADD)
		{
			writer.addString(nbtString != null ? nbtString : "");
			writer.addVec3(position != null ? position : Vec3.ZERO);
		}
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		switch (type)
		{
			case ADD:
				return executeAdd(ctx);

			case PLAYER_DISMOUNT:
				ctx.getEntity().stopRiding();
				return Result.OK;

			case NONE:
				return Result.IGNORED;
		}

		MocapActionContext.EntityData entityData = ctx.getEntityData(id);
		if (entityData == null) { return Result.IGNORED; }
		Entity entity = entityData.entity;

		switch (type)
		{
			case REMOVE:
				entity.remove(Entity.RemovalReason.KILLED);
				return Result.OK;

			case KILL:
				entity.invulnerableTime = 0; // for sound effect
				entity.kill(ctx.getLevel());
				return Result.OK;

			case HURT:
				Hurt.hurtEntity(entity, ctx.getConfig());
				return Result.OK;

			case PLAYER_MOUNT:
				ctx.getEntity().startRiding(entity, true, true);
				return Result.OK;
		}
		return Result.IGNORED;
	}

	private Result executeAdd(MocapActionContext ctx)
	{
		MocapEntityFilter filter = ctx.getModifiers().getEntityFilter();
		if (nbtString == null || position == null || ctx.hasEntity(id) || filter.isEmpty()) { return Result.IGNORED; }

		CompoundTag compoundTag;
		try
		{
			compoundTag = Utils.nbtFromString(nbtString);
		}
		catch (Exception e)
		{
			Utils.exception(e, "Exception occurred when parsing entity NBT data!");
			return Result.ERROR;
		}
		ValueInput nbt = TagValueInput.create(ProblemReporter.DISCARDING, ctx.getEntity().registryAccess(), compoundTag);

		Entity entity = EntityType.create(nbt, ctx.getLevel(), EntitySpawnReason.MOB_SUMMONED).orElse(null);
		if (entity == null || !filter.isAllowed(entity)) { return Result.IGNORED; }

		entity.setPos(ctx.getTransformer().transformPos(position));
		entity.setDeltaMovement(0.0, 0.0, 0.0);
		entity.setNoGravity(true);
		entity.setInvulnerable(ctx.getConfig().getInvulnerablePlayback());
		entity.addTag(PlaybackManager.MOCAP_ENTITY_TAG);
		if (entity instanceof Mob) { ((Mob)entity).setNoAi(true); }
		ctx.getModifiers().getTransformations().applyScaleToEntity(entity);

		ctx.getLevel().addFreshEntity(entity);
		ctx.addEntity(id, entity, position);
		return Result.OK;
	}

	public enum UpdateType
	{
		NONE(0),
		ADD(1),
		REMOVE(2),
		KILL(3),
		HURT(4),
		PLAYER_MOUNT(5),
		PLAYER_DISMOUNT(6);

		private static final UpdateType[] VALUES = values();
		private final byte id;

		UpdateType(int id)
		{
			this.id = (byte)id;
		}

		private static UpdateType fromId(byte id)
		{
			for (UpdateType type : VALUES)
			{
				if (type.id == id) { return type; }
			}
			return NONE;
		}
	}
}
