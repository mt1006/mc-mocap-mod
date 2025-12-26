package net.mt1006.mocap.mocap.actions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.controller.config.MocapNbtRecordingMode;
import net.mt1006.mocap.api.v1.controller.config.MocapRecordingConfig;
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

	public static EntityUpdate addEntity(int id, Entity entity, MocapRecordingConfig config)
	{
		String nbtString = serializeEntityNBT(entity, config).toString();
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

	public static CompoundTag serializeEntityNBT(Entity entity, MocapRecordingConfig config)
	{
		TagValueOutput tagOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());

		String id = ((EntityIdFields)entity).callGetEncodeId();
		tagOutput.putString("id", id != null ? id : "minecraft:cow");
		if (config.getNbtRecordingMode() != MocapNbtRecordingMode.DISABLED) entity.saveWithoutId(tagOutput);

		CompoundTag nbt = tagOutput.buildResult();
		nbt.remove("UUID");
		nbt.remove("Pos");
		nbt.remove("Motion");

		if (config.getNbtRecordingMode() == MocapNbtRecordingMode.FILTERED) { filterEntityNBT(nbt, entity); }
		return nbt;
	}

	public static void filterEntityNBT(CompoundTag nbt, Entity entity)
	{
		nbt.remove("Brain");
		nbt.remove("EggLayTime");
		nbt.remove("CanPickUpLoot");
		nbt.remove("NoAI");
		nbt.remove("ForcedAge");
		nbt.remove("EggLayTime");
		nbt.remove("fall_distance");
		if (nbt.getShortOr("HurtTime", (short)-1) == 0) { nbt.remove("HurtTime"); }
		if (nbt.getShortOr("DeathTime", (short)-1) == 0) { nbt.remove("DeathTime"); }
		if (nbt.getIntOr("HurtByTimestamp", -1) == 0) { nbt.remove("HurtByTimestamp"); }
		if (nbt.getShortOr("Air", (short)-1) == entity.getMaxAirSupply()) { nbt.remove("Air"); }
		if (nbt.getFloatOr("AbsorptionAmount", -1.0f) == 0.0f) { nbt.remove("AbsorptionAmount"); }
		if (entity instanceof LivingEntity living && nbt.getFloatOr("Health", -1.0f) == living.getMaxHealth()) { nbt.remove("Health"); }

		if (nbt.getIntOr("Age", -1) >= 0) { nbt.remove("Age"); }
		else { nbt.putInt("Age", -9); } // -9 is short in string form and is enough time for playback to set IS_BABY flag

		ListTag listTag = nbt.getList("attributes").orElse(null);
		if (listTag != null)
		{
			ListTag newListTag = new ListTag();
			for (Tag tag : listTag)
			{
				CompoundTag attribute = tag.asCompound().orElse(null);
				String attributeIdStr = attribute != null ? attribute.getString("id").orElse(null) : null;
				ResourceLocation attributeId = attributeIdStr != null ? ResourceLocation.tryParse(attributeIdStr) : null;

				if (attributeId == null)
				{
					// this means attribute list is broken, but keep it anyway
					newListTag.add(tag);
					continue;
				}

				if (Attributes.FOLLOW_RANGE.is(attributeId)) { continue; }
				if (Attributes.ATTACK_DAMAGE.is(attributeId)) { continue; }
				if (Attributes.FALL_DAMAGE_MULTIPLIER.is(attributeId)) { continue; }
				if (Attributes.SAFE_FALL_DISTANCE.is(attributeId)) { continue; }
				newListTag.add(tag);
			}

			if (newListTag.isEmpty()) { nbt.remove("attributes"); }
			else { nbt.put("attributes", newListTag); }
		}
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
				//TODO: fix slime splitting into dummy slimes
				entity.invulnerableTime = 0; // for sound effect
				entity.kill(ctx.getLevel());
				return Result.OK;

			case HURT:
				Hurt.hurtEntity(entity, ctx.getConfig());
				return Result.OK;

			case PLAYER_MOUNT:
				ctx.getEntity().startRiding(entity, true);
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
