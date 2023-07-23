package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class SetMainHand implements ComparableAction
{
	private final HumanoidArm mainHand;

	public SetMainHand(Entity entity)
	{
		mainHand = entity instanceof LivingEntity ? ((LivingEntity)entity).getMainArm() : HumanoidArm.RIGHT;
	}

	public SetMainHand(RecordingFiles.Reader reader)
	{
		mainHand = reader.readBoolean() ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
	}

	@Override public boolean differs(ComparableAction action)
	{
		return mainHand != ((SetMainHand)action).mainHand;
	}

	@Override public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action)
	{
		if (action != null && !differs(action)) { return; }
		writer.addByte(Type.SET_MAIN_HAND.id);
		writer.addBoolean(mainHand == HumanoidArm.RIGHT);
	}

	@Override public Result execute(PlayingContext ctx)
	{

		if (ctx.entity instanceof Player) { ((Player)ctx.entity).setMainArm(mainHand); }
		else if (ctx.entity instanceof Mob) { ((Mob)ctx.entity).setLeftHanded(mainHand == HumanoidArm.LEFT); }
		else { return Result.IGNORED; }
		return Result.OK;
	}
}
