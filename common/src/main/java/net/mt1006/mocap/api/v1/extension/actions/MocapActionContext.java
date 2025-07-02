package net.mt1006.mocap.api.v1.extension.actions;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface MocapActionContext extends MocapBasicActionContext
{
	@Nullable ServerPlayer getDummyPlayer();

	@Nullable ServerPlayer getRealOrDummyPlayer();

	@Nullable LivingEntity getLivingEntityOrDummyPlayer();

	void setMainContextEntity();

	boolean setContextEntity(int id);

	void broadcast(Packet<?> packet);

	void fluentMovement(Supplier<Packet<?>> packetSupplier);

	Vec3 getPosition();

	void changePosition(Vec3 newPos, float rotY, float rotX, boolean transformRot);

	void addEntity(int id, Entity entity, Vec3 position);

	@Nullable EntityData getEntityData(int id);

	boolean hasEntity(int id);

	void incrementRepeatCounter();

	boolean shouldStopRepeat(int iter);

	class EntityData
	{
		public final Entity entity;
		public Vec3 lastPosition;

		public EntityData(Entity entity, Vec3 startPos)
		{
			this.entity = entity;
			this.lastPosition = startPos;
		}
	}
}
