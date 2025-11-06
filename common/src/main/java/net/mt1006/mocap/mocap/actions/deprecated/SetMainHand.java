package net.mt1006.mocap.mocap.actions.deprecated;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;

public class SetMainHand implements MocapAction
{
	private final HumanoidArm mainHand;

	public SetMainHand(Reader reader)
	{
		mainHand = reader.readBoolean() ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		//TODO: [CONVERTER] remove
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
