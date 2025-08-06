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
	public static final Respawn INSTANCE = new Respawn();

	private Respawn() {}

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
