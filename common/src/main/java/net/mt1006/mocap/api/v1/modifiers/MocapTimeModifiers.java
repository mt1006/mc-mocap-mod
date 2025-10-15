package net.mt1006.mocap.api.v1.modifiers;

import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.files.SceneFiles;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface MocapTimeModifiers
{
	MocapTime getStartDelay();

	MocapTimeModifiers withStartDelay(MocapTime val);

	MocapTime getWaitOnStart();

	MocapTimeModifiers withWaitOnStart(MocapTime val);

	MocapTime getWaitOnEnd();

	MocapTimeModifiers withWaitOnEnd(MocapTime val);

	boolean getWaitForParentEnd();

	MocapTimeModifiers withWaitForParentEnd(boolean val);

	boolean getLoop();

	MocapTimeModifiers withLoop(boolean val);

	@ApiStatus.Internal
	int getWaitTicksSum();

	@ApiStatus.Internal
	boolean areDefault();

	@ApiStatus.Internal
	MocapTimeModifiers mergeWithParent(MocapTimeModifiers parent);

	@ApiStatus.Internal
	@Nullable SceneFiles.Writer save();

	@ApiStatus.Internal
	void list(CommandOutput out);

	@ApiStatus.Internal
	@Nullable MocapTimeModifiers modify(FullCommandInfo info, int propertyNodePos);

}
