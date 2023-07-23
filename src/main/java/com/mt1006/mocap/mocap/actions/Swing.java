package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

public class Swing implements ComparableAction
{
	private final boolean swinging;
	private final int swingingTime;
	private final Hand hand;

	public Swing(Entity entity)
	{
		if (entity instanceof LivingEntity)
		{
			LivingEntity livingEntity = (LivingEntity)entity;
			swinging = livingEntity.swinging;
			swingingTime = livingEntity.swingTime;
			hand = livingEntity.swingingArm;
		}
		else
		{
			swinging = false;
			swingingTime = 0;
			hand = Hand.MAIN_HAND;
		}
	}

	public Swing(RecordingFiles.Reader reader)
	{
		swinging = true;
		swingingTime = 0;
		hand = reader.readBoolean() ? Hand.OFF_HAND : Hand.MAIN_HAND;
	}

	@Override public boolean differs(ComparableAction action)
	{
		return swinging != ((Swing)action).swinging;
	}

	@Override public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action)
	{
		if (swinging && (action == null || !((Swing)action).swinging || ((Swing)action).swingingTime > swingingTime))
		{
			writer.addByte(Type.SWING.id);
			writer.addBoolean(hand == Hand.OFF_HAND);
		}
	}

	@Override public Result execute(PlayingContext ctx)
	{
		if (!(ctx.entity instanceof LivingEntity)) { return Result.IGNORED; }
		((LivingEntity)ctx.entity).swing(hand);
		return Result.OK;
	}
}
