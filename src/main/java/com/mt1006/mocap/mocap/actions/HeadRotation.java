package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.SEntityHeadLookPacket;
import org.jetbrains.annotations.Nullable;

public class HeadRotation implements ComparableAction
{
	private final float headRotY;

	public HeadRotation(Entity entity)
	{
		headRotY = entity.getYHeadRot();
	}

	public HeadRotation(RecordingFiles.Reader reader)
	{
		headRotY = reader.readFloat();
	}

	@Override public boolean differs(ComparableAction action)
	{
		return headRotY != ((HeadRotation)action).headRotY;
	}

	@Override public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action)
	{
		if (action != null && !differs(action)) { return; }
		writer.addByte(Type.HEAD_ROTATION.id);
		writer.addFloat(headRotY);
	}

	@Override public Result execute(PlayingContext ctx)
	{
		ctx.entity.setYHeadRot(headRotY);
		ctx.fluentMovement(() -> new SEntityHeadLookPacket(ctx.entity, (byte)Math.floor(headRotY * 256.0f / 360.0f)));
		return Result.OK;
	}
}
