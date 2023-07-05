package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;

public class NextTick implements Action
{
	public NextTick() {}

	public NextTick(RecordingFiles.Reader ignored) {}

	public void write(RecordingFiles.Writer writer)
	{
		writer.addByte(Type.NEXT_TICK.id);
	}

	@Override public Result execute(PlayingContext ctx)
	{
		return Result.NEXT_TICK;
	}
}
