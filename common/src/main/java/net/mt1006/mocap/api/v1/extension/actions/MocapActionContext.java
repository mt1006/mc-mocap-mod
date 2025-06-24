package net.mt1006.mocap.api.v1.extension.actions;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.extension.MocapPositionTransformer;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface MocapActionContext
{
	Entity getEntity();

	ServerLevel getLevel();

	MocapModifiers getModifiers();

	MocapPositionTransformer getTransformer();

	@Nullable ServerPlayer getDummy();

	@Nullable ServerPlayer getPlayerOrDummy();

	@Nullable LivingEntity getLivingEntityOrDummy();

	void setMainContextEntity();

	boolean setContextEntity(int id);

	void broadcast(Packet<?> packet);

	void fluentMovement(Supplier<Packet<?>> packetSupplier);

	void changePosition(Vec3 newPos, float rotY, float rotX, boolean shiftXZ, boolean shiftY, boolean transformRot);

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
