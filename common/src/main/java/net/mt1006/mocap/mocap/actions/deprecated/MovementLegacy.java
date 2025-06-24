package net.mt1006.mocap.mocap.actions.deprecated;

import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.mixin.fields.EntityFields;

public class MovementLegacy implements MocapAction
{
	//TODO: test with legacy recordings
	private final Vec3 position;
	private final float[] rotation = new float[2];
	private final boolean isOnGround;

	public MovementLegacy(Reader reader)
	{
		position = reader.readVec3();

		rotation[0] = reader.readFloat();
		rotation[1] = reader.readFloat();

		isOnGround = reader.readBoolean();
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		throw new RuntimeException("Trying to save deprecated action!");
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		ctx.changePosition(position, rotation[1], rotation[0], true, true, true);

		ctx.getEntity().setOnGround(isOnGround);
		((EntityFields)ctx.getEntity()).callCheckInsideBlocks();
		ctx.fluentMovement(() -> new ClientboundTeleportEntityPacket(ctx.getEntity()));
		return Result.OK;
	}
}
