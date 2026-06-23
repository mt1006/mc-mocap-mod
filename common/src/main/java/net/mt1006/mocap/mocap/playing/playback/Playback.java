package net.mt1006.mocap.mocap.playing.playback;

import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.api.v1.modifiers.MocapTimeModifiers;
import net.mt1006.mocap.command.CommandsContext;
import net.mt1006.mocap.mocap.recording.RecordingContext;
import net.mt1006.mocap.mocap.recording.RecordingManager;
import org.jetbrains.annotations.Nullable;

public abstract class Playback
{
	//TODO: "playback.start.error.loop" and "playback.start.error.load" as failures, not errors

	protected final boolean isRoot;
	public final @Nullable ServerPlayer owner;
	public final MocapPlaybackConfig config;
	protected boolean finished = false, stopped = false;
	protected final MocapModifiers modifiers;
	protected int tickCounter = 0; //TODO: StartContext?
	protected int waitOnEnd = 0;

	protected Playback(boolean isRoot, @Nullable ServerPlayer owner, MocapPlaybackConfig config, MocapModifiers modifiers)
	{
		this.isRoot = isRoot;
		this.owner = owner;
		this.config = config;
		this.modifiers = modifiers;
	}

	public void tick()
	{
		if (finished) { return; }

		if (shouldExecuteTick())
		{
			if (waitOnEnd != 0)
			{
				if (waitOnEnd == 1) { finished = true; }
				waitOnEnd--;
			}
			else
			{
				executeTick();
			}
			tickCounter++;
		}

		if (finished && modifiers.getTimeModifiers().getLoop()) { loop(); }
		else if (shouldSelfStop()) { stop(); }
	}

	protected abstract void executeTick();

	public abstract void stop();

	protected abstract void loop();

	protected abstract boolean shouldSelfStop();

	protected boolean isActive()
	{
		if (stopped) { return false; }

		MocapTimeModifiers timeModifiers = modifiers.getTimeModifiers();
		if (timeModifiers.getLoop()) { return !timeModifiers.getWaitForParentEnd(); }
		return !finished;
	}

	protected boolean shouldExecuteTick()
	{
		if (CommandsContext.haveSyncEnabled == 0 || owner == null) { return true; }

		CommandsContext commandsContext = CommandsContext.get(owner);
		if (!commandsContext.getSync()) { return true; }

		for (RecordingContext ctx : RecordingManager.bySourcePlayer(owner))
		{
			if (ctx.getState() == RecordingContext.State.RECORDING) { return true; }
		}
		return false;
	}

	protected void finishOrWaitOnEnd()
	{
		int timeWaitOnEnd = modifiers.getTimeModifiers().getWaitOnEnd().ticks;
		if (timeWaitOnEnd == 0) { finished = true; }
		else { waitOnEnd = timeWaitOnEnd; }
	}
}
