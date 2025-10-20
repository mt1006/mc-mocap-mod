package net.mt1006.mocap.mocap.actions;

import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.command.converter.AlphaConverter;
import org.jetbrains.annotations.Nullable;

public class EntityAction implements MocapAction
{
	private final int id;
	private final MocapAction action;

	public EntityAction(int id, MocapAction action)
	{
		this.id = id;
		this.action = action;
	}

	//TODO: [CONVERTER] remove
	public EntityAction(Reader reader, MocapRecordingData data)
	{
		this(reader, data, null);
	}

	//TODO: [CONVERTER] remove last arg
	public EntityAction(Reader reader, MocapRecordingData data, @Nullable AlphaConverter converter)
	{
		this.id = reader.readInt();

		MocapAction action = ActionType.readAction(reader, data, converter, id);
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
