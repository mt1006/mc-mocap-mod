package net.mt1006.mocap.mocap.actions;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapStateAction;

import java.util.EnumMap;

public class ChangePose implements MocapStateAction
{
	private static final BiMap<Integer, Pose> poseMap;
	private static final BiMap<Pose, Integer> poseIdMap;
	static
	{
		EnumMap<Pose, Integer> enumMap = new EnumMap<>(Pose.class);
		enumMap.put(Pose.STANDING, 1);
		enumMap.put(Pose.FALL_FLYING, 2);
		enumMap.put(Pose.SLEEPING, 3);
		enumMap.put(Pose.SWIMMING, 4);
		enumMap.put(Pose.SPIN_ATTACK, 5);
		enumMap.put(Pose.CROUCHING, 6);
		enumMap.put(Pose.DYING, 7);
		enumMap.put(Pose.LONG_JUMPING, 8);
		enumMap.put(Pose.CROAKING, 9);
		enumMap.put(Pose.USING_TONGUE, 10);
		enumMap.put(Pose.SITTING, 11);
		enumMap.put(Pose.ROARING, 12);
		enumMap.put(Pose.SNIFFING, 13);
		enumMap.put(Pose.EMERGING, 14);
		enumMap.put(Pose.DIGGING, 15);
		enumMap.put(Pose.SLIDING, 16);
		enumMap.put(Pose.SHOOTING, 17);
		enumMap.put(Pose.INHALING, 18);

		poseIdMap = HashBiMap.create(enumMap);
		poseMap = poseIdMap.inverse();
	}

	private final Pose pose;

	public ChangePose(Entity entity)
	{
		pose = entity.getPose();
	}

	public ChangePose(Reader reader)
	{
		pose = poseMap.getOrDefault(reader.readInt(), Pose.STANDING);
	}

	@Override public boolean differs(MocapStateAction previousAction)
	{
		return pose != ((ChangePose)previousAction).pose;
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addInt(poseIdMap.getOrDefault(pose, 0));
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		ctx.getEntity().setPose(pose);
		return Result.OK;
	}
}
