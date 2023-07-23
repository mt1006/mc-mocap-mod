package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import org.jetbrains.annotations.Nullable;

public class SetMainHand implements ComparableAction
{
	private final HandSide mainHand;

	public SetMainHand(Entity entity)
	{
		mainHand = entity instanceof LivingEntity ? ((LivingEntity)entity).getMainArm() : HandSide.RIGHT;
	}

	public SetMainHand(RecordingFiles.Reader reader)
	{
		mainHand = reader.readBoolean() ? HandSide.RIGHT : HandSide.LEFT;
	}

	@Override public boolean differs(ComparableAction action)
	{
		return mainHand != ((SetMainHand)action).mainHand;
	}

	@Override public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action)
	{
		if (action != null && !differs(action)) { return; }
		writer.addByte(Type.SET_MAIN_HAND.id);
		writer.addBoolean(mainHand == HandSide.RIGHT);
	}

	@Override public Result execute(PlayingContext ctx)
	{

		if (ctx.entity instanceof PlayerEntity) { ((PlayerEntity)ctx.entity).setMainArm(mainHand); }
		else if (ctx.entity instanceof MobEntity) { ((MobEntity)ctx.entity).setLeftHanded(mainHand == HandSide.LEFT); }
		else { return Result.IGNORED; }
		return Result.OK;
	}
}
