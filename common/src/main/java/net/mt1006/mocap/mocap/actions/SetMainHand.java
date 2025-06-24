package net.mt1006.mocap.mocap.actions;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapStateAction;

public class SetMainHand implements MocapStateAction
{
	private final HumanoidArm mainHand;

	public SetMainHand(Entity entity)
	{
		mainHand = entity instanceof LivingEntity ? ((LivingEntity)entity).getMainArm() : HumanoidArm.RIGHT;
	}

	public SetMainHand(Reader reader)
	{
		mainHand = reader.readBoolean() ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
	}

	@Override public boolean differs(MocapStateAction previousAction)
	{
		return mainHand != ((SetMainHand)previousAction).mainHand;
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addBoolean(mainHand == HumanoidArm.RIGHT);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		//TODO: test how it affects ghost player RightClickBlock
		if (ctx.getEntity() instanceof Player) { ((Player)ctx.getEntity()).setMainArm(mainHand); }
		else if (ctx.getEntity() instanceof Mob) { ((Mob)ctx.getEntity()).setLeftHanded(mainHand == HumanoidArm.LEFT); }
		else { return Result.IGNORED; }
		return Result.OK;
	}
}
