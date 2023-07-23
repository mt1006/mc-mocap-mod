package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import com.mt1006.mocap.utils.EntityData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

public class SetLivingEntityFlags implements ComparableAction
{
	private final byte livingEntityFlags;

	public SetLivingEntityFlags(Entity entity)
	{
		if (!(entity instanceof LivingEntity))
		{
			livingEntityFlags = 0;
			return;
		}
		LivingEntity livingEntity = (LivingEntity)entity;

		byte livingEntityFlags = 0;
		if (livingEntity.isUsingItem()) { livingEntityFlags |= 0x01; }
		if (livingEntity.getUsedItemHand() == Hand.OFF_HAND) { livingEntityFlags |= 0x02; }
		if (livingEntity.isAutoSpinAttack()) { livingEntityFlags |= 0x04; }
		this.livingEntityFlags = livingEntityFlags;
	}

	public SetLivingEntityFlags(RecordingFiles.Reader reader)
	{
		livingEntityFlags = reader.readByte();
	}

	@Override public boolean differs(ComparableAction action)
	{
		return livingEntityFlags != ((SetLivingEntityFlags)action).livingEntityFlags;
	}

	@Override public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action)
	{
		if (action != null && !differs(action)) { return; }
		writer.addByte(Type.SET_LIVING_ENTITY_FLAGS.id);
		writer.addByte(livingEntityFlags);
	}

	@Override public Result execute(PlayingContext ctx)
	{
		if (!(ctx.entity instanceof LivingEntity)) { return Result.IGNORED; }
		EntityData.LIVING_ENTITY_FLAGS.set(ctx.entity, livingEntityFlags);
		return Result.OK;
	}
}
