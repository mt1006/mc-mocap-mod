package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface ComparableAction extends Action
{
	List<Function<Entity, ComparableAction>> REGISTERED = new ArrayList<>();
	Dummy DUMMY = new Dummy();

	boolean differs(ComparableAction action);

	void write(RecordingFiles.Writer writer, @Nullable ComparableAction action);

	class Dummy implements ComparableAction
	{
		@Override public Result execute(PlayingContext ctx) { return Result.IGNORED; }
		@Override public boolean differs(ComparableAction action) { return false; }
		@Override public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action) {}
	}
}
