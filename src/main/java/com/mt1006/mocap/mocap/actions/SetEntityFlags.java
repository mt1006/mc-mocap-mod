package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import com.mt1006.mocap.utils.EntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class SetEntityFlags implements ComparableAction
{
	private final byte entityFlags;

	public SetEntityFlags(Entity entity)
	{
		byte entityFlags = 0;
		if (entity.isOnFire()) { entityFlags |= 0x01; }
		if (entity.isShiftKeyDown()) { entityFlags |= 0x02; }
		if (entity.isSprinting()) { entityFlags |= 0x08; }
		if (entity.isSwimming()) { entityFlags |= 0x10; }
		if (entity.isInvisible()) { entityFlags |= 0x20; }
		if (entity.isCurrentlyGlowing()) { entityFlags |= 0x40; }
		if (entity instanceof LivingEntity && ((LivingEntity)entity).isFallFlying()) { entityFlags |= 0x80; }
		this.entityFlags = entityFlags;
	}

	public SetEntityFlags(RecordingFiles.Reader reader)
	{
		entityFlags = reader.readByte();
	}

	@Override public boolean differs(ComparableAction action)
	{
		return entityFlags != ((SetEntityFlags)action).entityFlags;
	}

	@Override public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action)
	{
		if (action != null && !differs(action)) { return; }
		writer.addByte(Type.SET_ENTITY_FLAGS.id);
		writer.addByte(entityFlags);
	}

	@Override public Result execute(PlayingContext ctx)
	{
		EntityData.ENTITY_FLAGS.set(ctx.entity, entityFlags);
		return Result.OK;
	}
}
