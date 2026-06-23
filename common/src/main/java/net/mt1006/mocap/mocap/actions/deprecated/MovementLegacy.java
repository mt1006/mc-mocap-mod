package net.mt1006.mocap.mocap.actions.deprecated;

import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;

import java.util.Set;

public class MovementLegacy implements MocapAction
{
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
		Entity entity = ctx.getEntity();
		Vec3 oldPos = entity.position();
		ctx.changePosition(ctx.getPosition().add(position), rotation[1], rotation[0], true);

		entity.setOnGround(isOnGround);
		entity.applyEffectsFromBlocks(oldPos, entity.position());
		ctx.fluentMovement(() -> new ClientboundTeleportEntityPacket(entity.getId(), PositionMoveRotation.of(entity), Set.of(), isOnGround));
		return Result.OK;
	}
}
