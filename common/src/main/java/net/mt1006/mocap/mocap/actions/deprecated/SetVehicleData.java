package net.mt1006.mocap.mocap.actions.deprecated;

import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.mocap.actions.SetNonPlayerEntityData;
import org.jetbrains.annotations.Nullable;

public class SetVehicleData implements MocapAction
{
	private final @Nullable SetNonPlayerEntityData entityData;

	public SetVehicleData(Reader reader)
	{
		if (!reader.readBoolean())
		{
			entityData = null;
			return;
		}

		byte flags = reader.readByte();
		boolean flag1 = reader.readBoolean();
		boolean flag2 = reader.readBoolean();
		int int1 = reader.readInt();
		int int2 = reader.readInt();
		int int3 = reader.readInt();
		float float1 = reader.readFloat();
		entityData = new SetNonPlayerEntityData(flag1, flag2, flags, int1, int2, int3, float1);
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		throw new RuntimeException("Trying to save deprecated action!");
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		return entityData != null ? entityData.execute(ctx) : Result.IGNORED;
	}
}
