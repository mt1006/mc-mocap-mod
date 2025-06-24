package net.mt1006.mocap.mocap.actions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapStateAction;

public class Swing implements MocapStateAction
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

	public Swing(Reader reader)
	{
		swinging = true;
		swingingTime = 0;
		hand = reader.readBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
	}

	@Override public boolean differs(MocapStateAction previousAction)
	{
		Swing previousSwing = (Swing)previousAction;
		return swinging && (previousSwing == null || !previousSwing.swinging || previousSwing.swingingTime > swingingTime);
	}

	@Override public boolean shouldBeInitialized()
	{
		return false;
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addBoolean(hand == InteractionHand.OFF_HAND);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (!(ctx.getEntity() instanceof LivingEntity)) { return Result.IGNORED; }
		((LivingEntity)ctx.getEntity()).swing(hand);
		return Result.OK;
	}
}
