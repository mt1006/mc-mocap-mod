package net.mt1006.mocap.mocap.actions.deprecated;

import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;

public class HeadRotation implements MocapAction
{
	private final float headRotY;

	public HeadRotation(Reader reader)
	{
		headRotY = reader.readFloat();
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		throw new RuntimeException("Trying to save deprecated action!");
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		float finHeadRot = ctx.getTransformer().transformRotation(headRotY);
		ctx.getEntity().setYHeadRot(finHeadRot);
		ctx.fluentMovement(() -> new ClientboundRotateHeadPacket(ctx.getEntity(), (byte)Math.floor(finHeadRot * 256.0f / 360.0f)));
		return Result.OK;
	}
}
