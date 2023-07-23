package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class Swing implements ComparableAction
{
	private final boolean swinging;
	private final int swingingTime;
	private final InteractionHand hand;

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
			hand = InteractionHand.MAIN_HAND;
		}
	}

	public Swing(RecordingFiles.Reader reader)
	{
		swinging = true;
		swingingTime = 0;
		hand = reader.readBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
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
			writer.addBoolean(hand == InteractionHand.OFF_HAND);
		}
	}

	@Override public Result execute(PlayingContext ctx)
	{
		if (!(ctx.entity instanceof LivingEntity)) { return Result.IGNORED; }
		((LivingEntity)ctx.entity).swing(hand);
		return Result.OK;
	}
}
