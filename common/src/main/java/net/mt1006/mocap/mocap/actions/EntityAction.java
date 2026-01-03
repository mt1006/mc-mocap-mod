package net.mt1006.mocap.mocap.actions;

import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;

public class EntityAction implements MocapAction
{
	private final int id;
	private final MocapAction action;

	public EntityAction(int id, MocapAction action)
	{
		this.id = id;
		this.action = action;
	}

	public EntityAction(Reader reader, MocapRecordingData data)
	{
		this.id = reader.readInt();

		MocapAction action = ActionType.readAction(reader, data);
		this.action = action != null ? action : DummyAction.INSTANCE;
	}

	@Override public void prepareWrite(MocapRecordingData data)
	{
		action.prepareWrite(data);
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addInt(id);
		ActionType.writeAction(writer, data, action);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (!ctx.setContextEntity(id)) { return Result.IGNORED; }
		Result retVal = action.execute(ctx);
		ctx.setMainContextEntity();
		return retVal;
	}
}
