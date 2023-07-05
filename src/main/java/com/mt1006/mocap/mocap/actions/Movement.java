package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mixin.fields.EntityMixin;
import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class Movement implements ComparableAction
{
	private final double[] position = new double[3];
	private final float[] rotation = new float[2];
	private final boolean isOnGround;

	public Movement(Entity entity)
	{
		Vec3 entityPos = entity.position();
		position[0] = entityPos.x;
		position[1] = entityPos.y;
		position[2] = entityPos.z;

		rotation[0] = entity.getXRot();
		rotation[1] = entity.getYRot();

		isOnGround = entity.isOnGround();
	}

	public Movement(RecordingFiles.Reader reader)
	{
		position[0] = reader.readDouble();
		position[1] = reader.readDouble();
		position[2] = reader.readDouble();

		rotation[0] = reader.readFloat();
		rotation[1] = reader.readFloat();

		isOnGround = reader.readBoolean();
	}

	public void writeAsHeader(RecordingFiles.Writer writer)
	{
		writer.addDouble(position[0]);
		writer.addDouble(position[1]);
		writer.addDouble(position[2]);

		writer.addFloat(rotation[1]);
		writer.addFloat(rotation[0]);
	}

	@Override public boolean differs(ComparableAction action)
	{
		return !Arrays.equals(position, ((Movement)action).position) ||
				!Arrays.equals(rotation, ((Movement)action).rotation) ||
				isOnGround != ((Movement)action).isOnGround;
	}

	@Override public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action)
	{
		if (action != null && !differs(action)) { return; }
		writer.addByte(Type.MOVEMENT.id);

		if (action != null)
		{
			writer.addDouble(position[0] - ((Movement)action).position[0]);
			writer.addDouble(position[1] - ((Movement)action).position[1]);
			writer.addDouble(position[2] - ((Movement)action).position[2]);
		}
		else
		{
			writer.addDouble(0.0);
			writer.addDouble(0.0);
			writer.addDouble(0.0);
		}

		writer.addFloat(rotation[0]);
		writer.addFloat(rotation[1]);

		writer.addBoolean(isOnGround);
	}

	@Override public Result execute(PlayingContext ctx)
	{
		//TODO: switch from relative to absolute
		if (ctx.entity == ctx.mainEntity)
		{
			ctx.shiftPosition(position[0], position[1], position[2], rotation[1], rotation[0]);
		}
		else
		{
			double x = ctx.entity.getX() + position[0];
			double y = ctx.entity.getY() + position[1];
			double z = ctx.entity.getZ() + position[2];
			ctx.entity.moveTo(x, y, z, rotation[1], rotation[0]);
		}

		ctx.entity.setOnGround(isOnGround);
		((EntityMixin)ctx.entity).callCheckInsideBlocks();

		ctx.broadcast(new ClientboundTeleportEntityPacket(ctx.entity));
		return Result.OK;
	}
}
