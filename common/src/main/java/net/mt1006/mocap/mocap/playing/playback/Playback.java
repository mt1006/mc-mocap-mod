package net.mt1006.mocap.mocap.playing.playback;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.command.CommandsContext;
import net.mt1006.mocap.mocap.playing.modifiers.PlaybackModifiers;
import net.mt1006.mocap.mocap.recording.RecordingContext;
import net.mt1006.mocap.mocap.recording.RecordingManager;
import org.jetbrains.annotations.Nullable;

public abstract class Playback
{
	//TODO: "playback.start.error.loop" and "playback.start.error.load" as failures, not errors

	protected final boolean isRoot;
	protected final ServerLevel level;
	public final @Nullable ServerPlayer owner;
	public final MocapPlaybackConfig config;
	protected boolean finished = false;
	protected final PlaybackModifiers modifiers;
	protected int tickCounter = 0; //TODO: StartContext?

	protected Playback(boolean isRoot, ServerLevel level, @Nullable ServerPlayer owner, MocapPlaybackConfig config, PlaybackModifiers modifiers)
	{
		this.isRoot = isRoot;
		this.level = level;
		this.owner = owner;
		this.config = config;
		this.modifiers = modifiers;
	}

	public abstract boolean tick();

	public abstract void stop();

	//TODO: remove?
	public abstract boolean wasFinished();

	protected abstract PositionTransformer getPosTransformer();

	protected boolean shouldExecuteTick()
	{
		if (tickCounter == 0) { return true; }

		if (modifiers.startDelay.ticks <= tickCounter)
		{
			if (CommandsContext.haveSyncEnabled == 0 || owner == null) { return true; }

			CommandsContext commandsContext = CommandsContext.get(owner);
			if (!commandsContext.getSync()) { return true; }

			for (RecordingContext ctx : RecordingManager.bySourcePlayer(owner))
			{
				if (ctx.state == RecordingContext.State.RECORDING) { return true; }
			}
		}
		return false;
	}
}
