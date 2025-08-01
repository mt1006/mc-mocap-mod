package net.mt1006.mocap.mocap.actions;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.mixin.fields.EntityFields;
import net.mt1006.mocap.utils.FakePlayer;

import java.util.List;
import java.util.UUID;

public class Respawn implements MocapAction
{
	/*private final Vec3 pos;
	private final float rotY, rotX;
	private final @Nullable ResourceLocation dimensionId;

	public Respawn(Vec3 pos, float rotY, float rotX, @Nullable ResourceLocation dimensionId)
	{
		this.pos = pos;
		this.rotY = rotY;
		this.rotX = rotX;
		this.dimensionId = dimensionId;
	}

	public Respawn(RecordingFiles.Reader reader)
	{
		pos = reader.readVec3();
		rotY = reader.readFloat();
		rotX = reader.readFloat();

		//TODO: test when bad input
		String dimensionStr = reader.readString();
		dimensionId = dimensionStr.isEmpty() ? null : ResourceLocation.parse(dimensionStr);
	}

	@Override public void write(RecordingFiles.Writer writer)
	{
		writer.addByte(Type.RESPAWN.id);

		writer.addVec3(pos);
		writer.addFloat(rotY);
		writer.addFloat(rotX);
		writer.addString(dimensionId != null ? dimensionId.toString() : "");
	}*/

	public Respawn() {}

	public Respawn(Reader ignore) {}

	@Override public void write(Writer writer, MocapRecordingData data) {}

	@Override public Result execute(MocapActionContext ctx)
	{
		Entity entity = ctx.getEntity();
		entity.setPose(Pose.STANDING);

		if (entity instanceof LivingEntity livingEntity)
		{
			livingEntity.setHealth(livingEntity.getMaxHealth());
			livingEntity.deathTime = 0;
		}

		if (entity instanceof FakePlayer)
		{
			UUID uuid = entity.getUUID();
			ctx.broadcast(new ClientboundPlayerInfoRemovePacket(List.of(uuid)));
			ctx.getLevel().removePlayerImmediately((FakePlayer)entity, Entity.RemovalReason.KILLED);

			((FakePlayer)entity).fakeRespawn();
			ctx.getLevel().getServer().getPlayerList()
					.broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, (FakePlayer)entity));
			ctx.getLevel().addNewPlayer((FakePlayer)entity);
		}
		else
		{
			((EntityFields)entity).callUnsetRemoved();
			ctx.getLevel().addFreshEntity(entity);
		}
		return Result.OK;
	}
}
