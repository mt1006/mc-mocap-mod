package net.mt1006.mocap.api.impl.controller.playable;

import net.mt1006.mocap.api.impl.controller.MocapControllerImpl;
import net.mt1006.mocap.api.v1.controller.playable.MocapActiveRecording;
import net.mt1006.mocap.api.v1.controller.playable.MocapSavedRecording;
import net.mt1006.mocap.api.v1.extension.MocapActiveRecordingActions;
import net.mt1006.mocap.mocap.recording.Recording;
import net.mt1006.mocap.mocap.recording.RecordingContext;
import org.jetbrains.annotations.Nullable;

public class MocapActiveRecordingImpl extends MocapPlayableImpl implements MocapActiveRecording
{
	public final String id;
	public final @Nullable RecordingContext ctx;

	public MocapActiveRecordingImpl(MocapControllerImpl ctrl, RecordingContext ctx)
	{
		this(ctrl, ctx.id.str, ctx);
	}

	public MocapActiveRecordingImpl(MocapControllerImpl ctrl, String id)
	{
		this(ctrl, id, null);
	}

	private MocapActiveRecordingImpl(MocapControllerImpl ctrl, String id, @Nullable RecordingContext ctx)
	{
		super(ctrl);
		this.ctx = ctx != null ? ctx : Recording.resolveSingle(ctrl.commandInfo, id);
		this.id = this.ctx != null ? this.ctx.id.str : id;
		if (!getId().startsWith("-")) { throw new RuntimeException("Failed to create MocapActiveRecordingImpl!"); }
	}

	@Override public String getId()
	{
		return id;
	}

	@Override public boolean exists()
	{
		return ctx != null && !ctx.isRemoved();
	}

	@Override public boolean stop()
	{
		return ctx != null && Recording.stopSingle(ctrl.commandInfo, ctx);
	}

	@Override public boolean discard()
	{
		return ctx != null && Recording.discardSingle(ctrl.commandInfo, ctx);
	}

	@Override public @Nullable MocapSavedRecording save(String name)
	{
		boolean success = ctx != null && Recording.saveSingle(ctrl.commandInfo, ctx, name, false);
		return success ? new MocapSavedRecordingImpl(ctrl, name) : null;
	}

	@Override public MocapActiveRecordingActions getActions()
	{
		return ctx;
	}
}
