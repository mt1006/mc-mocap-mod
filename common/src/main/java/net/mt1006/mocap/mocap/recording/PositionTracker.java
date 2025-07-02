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
	private final Vec3 startPos;
	private Vec3 pos;
	private final float[] rotation = new float[2];
	private float headRot;
	private boolean onGround;
	private boolean forceNonPosDataFlag;

	public PositionTracker(Entity entity, boolean initRotAndGround, Vec3 startPos)
	{
		this.entity = entity;
		this.startPos = startPos;

		this.pos = entity.position();
		this.rotation[0] = entity.getXRot();
		this.rotation[1] = entity.getYRot();
		this.headRot = entity.getYHeadRot();
		this.onGround = entity.onGround();

		this.forceNonPosDataFlag = initRotAndGround;
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
		Movement movement = Movement.teleportToPos(FAR_AWAY, false);
		actionList.add(movement);

		pos = movement.getNewPosition(startPos, pos);
		rotation[0] = 0.0f;
		rotation[1] = 0.0f;
		headRot = 0.0f;
		onGround = false;
	}

	public @Nullable Movement getDelta()
	{
		return getDelta(false, false);
	}

	private @Nullable Movement getDelta(boolean applyChanges, boolean forceNonPosData)
	{
		float newXRot = entity.getXRot(), newYRot = entity.getYRot(), newHeadRot = entity.getYHeadRot();
		boolean newOnGround = entity.onGround();

		Movement movement = Movement.delta(startPos, pos, entity.position(), rotation,
				newXRot, newYRot, headRot, newHeadRot, onGround, newOnGround, forceNonPosData);

		if (applyChanges)
		{
			rotation[0] = newXRot;
			rotation[1] = newYRot;
			headRot = newHeadRot;
			onGround = newOnGround;
			if (movement != null) { pos = movement.getNewPosition(startPos, pos); }
		}
		return movement;
	}

	public void writeStartPos(RecordingData data)
	{
		data.startPos = startPos;

		// unlike in other places, file header has first rotY, than rotX
		data.startRot[0] = rotation[1];
		data.startRot[1] = rotation[0];
	}
}
