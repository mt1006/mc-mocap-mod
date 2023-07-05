package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class EntityAction implements Action
{
	private final int id;
	private final ComparableAction action;

	public EntityAction(int id, ComparableAction action)
	{
		this.id = id;
		this.action = action;
	}

	public EntityAction(RecordingFiles.Reader reader)
	{
		id = reader.readInt();
		Action readAction = Action.readAction(reader);
		if (readAction instanceof ComparableAction) { action = (ComparableAction)readAction; }
		else { action = ComparableAction.DUMMY; }
	}

	public void write(RecordingFiles.Writer writer, @Nullable ComparableAction previousAction)
	{
		RecordingFiles.Writer actionWriter = new RecordingFiles.Writer();
		action.write(actionWriter, previousAction);
		if (actionWriter.getByteList().size() == 0) { return; }

		writer.addByte(Type.ENTITY_ACTION.id);
		writer.addInt(id);
		writer.addWriter(actionWriter);
	}

	@Override public Result execute(PlayingContext ctx)
	{
		Entity entity = ctx.entityMap.get(id);
		if (entity == null) { return Result.IGNORED; }

		ctx.entity = entity;
		Result retVal = action.execute(ctx);
		ctx.entity = ctx.mainEntity;
		return retVal;
	}
}
