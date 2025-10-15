package net.mt1006.mocap.mocap.playing.modifiers;

import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.api.v1.modifiers.MocapTime;
import net.mt1006.mocap.api.v1.modifiers.MocapTimeModifiers;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.files.SceneFiles;
import org.jetbrains.annotations.Nullable;

public class TimeModifiers implements MocapTimeModifiers
{
	public static final TimeModifiers DEFAULT =
			new TimeModifiers(MocapTime.ZERO, MocapTime.ZERO, MocapTime.ZERO, true, false);
	private final MocapTime startDelay;
	private final MocapTime waitOnStart;
	private final MocapTime waitOnEnd;
	private final boolean waitForParentEnd;
	private final boolean loop;

	private TimeModifiers(MocapTime startDelay, MocapTime waitOnStart, MocapTime waitOnEnd, boolean waitForParentEnd, boolean loop)
	{
		this.startDelay = startDelay;
		this.waitOnStart = waitOnStart;
		this.waitOnEnd = waitOnEnd;
		this.waitForParentEnd = waitForParentEnd;
		this.loop = loop;
	}

	private TimeModifiers(SceneFiles.Reader reader)
	{
		this.startDelay = MocapTime.fromSeconds(reader.readDouble("start_delay", 0.0));
		this.waitOnStart = MocapTime.fromSeconds(reader.readDouble("wait_on_start", 0.0));
		this.waitOnEnd = MocapTime.fromSeconds(reader.readDouble("wait_on_end", 0.0));
		this.waitForParentEnd = reader.readBoolean("wait_for_parent_end", true);
		this.loop = reader.readBoolean("loop", false);
	}

	public static TimeModifiers fromObject(@Nullable SceneFiles.Reader reader)
	{
		return reader != null ? new TimeModifiers(reader) : DEFAULT;
	}

	public static MocapTimeModifiers fromLegacyScene(double waitOnStart)
	{
		return DEFAULT.withWaitOnStart(MocapTime.fromSeconds(waitOnStart));
	}

	@Override public MocapTime getStartDelay()
	{
		return startDelay;
	}

	@Override public MocapTimeModifiers withStartDelay(MocapTime val)
	{
		return new TimeModifiers(val, waitOnStart, waitOnEnd, waitForParentEnd, loop);
	}

	@Override public MocapTime getWaitOnStart()
	{
		return waitOnStart;
	}

	@Override public MocapTimeModifiers withWaitOnStart(MocapTime val)
	{
		return new TimeModifiers(startDelay, val, waitOnEnd, waitForParentEnd, loop);
	}

	@Override public MocapTime getWaitOnEnd()
	{
		return waitOnEnd;
	}

	@Override public MocapTimeModifiers withWaitOnEnd(MocapTime val)
	{
		return new TimeModifiers(startDelay, waitOnStart, val, waitForParentEnd, loop);
	}

	@Override public boolean getWaitForParentEnd()
	{
		return waitForParentEnd;
	}

	@Override public MocapTimeModifiers withWaitForParentEnd(boolean val)
	{
		return new TimeModifiers(startDelay, waitOnStart, waitOnEnd, val, loop);
	}

	@Override public boolean getLoop()
	{
		return loop;
	}

	@Override public MocapTimeModifiers withLoop(boolean val)
	{
		return new TimeModifiers(startDelay, waitOnStart, waitOnEnd, waitForParentEnd, val);
	}

	@Override public int getWaitTicksSum()
	{
		return startDelay.ticks + waitOnStart.ticks;
	}

	@Override public boolean areDefault()
	{
		return startDelay.ticks == 0 && waitOnStart.ticks == 0
				&& waitOnEnd.ticks == 0 && waitForParentEnd && !loop;
	}

	@Override public MocapTimeModifiers mergeWithParent(MocapTimeModifiers parent)
	{
		return new TimeModifiers(startDelay.add(parent.getStartDelay()),
				waitOnStart.add(parent.getWaitOnStart()),
				waitOnEnd,
				waitForParentEnd,
				loop);
	}

	@Override public @Nullable SceneFiles.Writer save()
	{
		if (areDefault()) { return null; }

		SceneFiles.Writer writer = new SceneFiles.Writer();
		writer.addDouble("start_delay", startDelay.seconds, 0.0);
		writer.addDouble("wait_on_start", waitOnStart.seconds, 0.0);
		writer.addDouble("wait_on_end", waitOnEnd.seconds, 0.0);
		writer.addBoolean("wait_for_parent_end", waitForParentEnd, true);
		writer.addBoolean("loop", loop, false);

		return writer;
	}

	@Override public void list(CommandOutput out)
	{
		out.sendSuccess("scenes.element_info.time.start_delay", startDelay.seconds, startDelay.ticks);
		out.sendSuccess("scenes.element_info.time.wait_on_start", waitOnStart.seconds, waitOnStart.ticks);
		out.sendSuccess("scenes.element_info.time.wait_on_end", waitOnEnd.seconds, waitOnEnd.ticks);
		out.sendSuccess("scenes.element_info.time.wait_for_parent_end." + waitForParentEnd);
		out.sendSuccess("scenes.element_info.time.loop." + loop);
	}

	@Override public @Nullable MocapTimeModifiers modify(FullCommandInfo info, int propertyNodePos)
	{
		switch (info.getNode(propertyNodePos))
		{
			case "start_delay":
				return withStartDelay(MocapTime.fromSeconds(info.getDouble("seconds")));

			case "wait_on_start":
				return withWaitOnStart(MocapTime.fromSeconds(info.getDouble("seconds")));

			case "wait_on_end":
				return withWaitOnEnd(MocapTime.fromSeconds(info.getDouble("seconds")));

			case "wait_for_parent_end":
				return withWaitForParentEnd(info.getBool("wait_for_parent_end"));

			case "loop":
				return withLoop(info.getBool("loop"));

			case null, default:
				return null;
		}
	}
}
