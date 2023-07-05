package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import com.mt1006.mocap.utils.EntityData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import org.jetbrains.annotations.Nullable;

public class SetMainHand implements ComparableAction
{
	private final byte mainHand;

	public SetMainHand(Entity entity)
	{
		if (entity instanceof LivingEntity)
		{
			if (((LivingEntity)entity).getMainArm() == HandSide.LEFT) { mainHand = 0; }
			else { mainHand = 1; }
		}
		else
		{
			mainHand = 1;
		}
	}

	public SetMainHand(RecordingFiles.Reader reader)
	{
		mainHand = reader.readByte();
	}

	@Override public boolean differs(ComparableAction action)
	{
		return mainHand != ((SetMainHand)action).mainHand;
	}

	@Override public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action)
	{
		if (action != null && !differs(action)) { return; }
		writer.addByte(Type.SET_MAIN_HAND.id);
		writer.addByte(mainHand);
	}

	@Override public Result execute(PlayingContext ctx)
	{
		if (ctx.entity instanceof PlayerEntity) { new EntityData(ctx.entity, EntityData.PLAYER_MAIN_HAND, mainHand).broadcast(ctx); }
		else if (ctx.entity instanceof MobEntity) { ((MobEntity)ctx.entity).setLeftHanded(mainHand == 0); }
		else { return Result.IGNORED; }
		return Result.OK;
	}
}
