package net.mt1006.mocap.mocap.recording;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.mocap.actions.EntityAction;
import net.mt1006.mocap.mocap.actions.Movement;
import net.mt1006.mocap.mocap.files.RecordingData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PositionTracker
{
	private final Vec3 FAR_AWAY = new Vec3(0.0, 1000000.0, 0.0);
	private Entity entity;
	private final double[] position = new double[3];
	private final float[] rotation = new float[2];
	private float headRot;
	private boolean isOnGround;
	private boolean forceNonPosDataFlag;

	public PositionTracker(Entity entity, boolean initRotAndGround)
	{
		this.entity = entity;

		Vec3 posVec = entity.position();
		position[0] = posVec.x;
		position[1] = posVec.y;
		position[2] = posVec.z;

		rotation[0] = entity.getXRot();
		rotation[1] = entity.getYRot();
		headRot = entity.getYHeadRot();
		isOnGround = entity.onGround();

		forceNonPosDataFlag = initRotAndGround;
	}

	public void setEntity(Entity entity)
	{
		this.entity = entity;
	}

	public void onTick(List<MocapAction> actionList, @Nullable Integer entityId)
	{
		Movement movement = getDelta(true, forceNonPosDataFlag);
		forceNonPosDataFlag = false;

		if (movement == null) { return; }
		actionList.add(entityId != null ? new EntityAction(entityId, movement) : movement);
	}

	public void teleportFarAway(List<MocapAction> actionList)
	{
		//TODO: replace pos arrays with Vec3
		Movement movement = Movement.teleportToPos(FAR_AWAY, false);
		actionList.add(movement);

		movement.applyToPosition(position);
		rotation[0] = 0.0f;
		rotation[1] = 0.0f;
		headRot = 0.0f;
		isOnGround = false;
	}

	public @Nullable Movement getDelta()
	{
		return getDelta(false, false);
	}

	private @Nullable Movement getDelta(boolean applyChanges, boolean forceNonPosData)
	{
		float newXRot = entity.getXRot(), newYRot = entity.getYRot(), newHeadRot = entity.getYHeadRot();
		boolean newIsOnGround = entity.onGround();

		Movement movement = Movement.delta(position, entity.position(), rotation,
				newXRot, newYRot, headRot, newHeadRot, isOnGround, newIsOnGround, forceNonPosData);

		if (applyChanges)
		{
			rotation[0] = newXRot;
			rotation[1] = newYRot;
			headRot = newHeadRot;
			isOnGround = newIsOnGround;
			if (movement != null) { movement.applyToPosition(position); }
		}

		return movement;
	}

	public void writeToRecordingData(RecordingData data)
	{
		data.startPos = new Vec3(position[0], position[1], position[2]);

		// unlike in other places, file header has first rotY, than rotX
		data.startRot[0] = rotation[1];
		data.startRot[1] = rotation[0];
	}
}
