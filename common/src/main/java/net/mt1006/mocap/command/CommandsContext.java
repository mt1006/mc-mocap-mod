package net.mt1006.mocap.command;

import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.mocap.playing.modifiers.PlaybackModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CommandsContext
{
	private static final Map<ServerPlayer, CommandsContext> contexts = new HashMap<>();
	public static int haveSyncEnabled = 0;
	public MocapModifiers modifiers = PlaybackModifiers.DEFAULT;
	private boolean sync = false;
	public @Nullable String doubleStart = null;

	public static CommandsContext get(ServerPlayer player)
	{
		CommandsContext ctx = contexts.get(player);
		if (ctx == null)
		{
			ctx = new CommandsContext();
			contexts.put(player, ctx);
		}
		return ctx;
	}

	public static void removePlayer(ServerPlayer player)
	{
		CommandsContext ctx = contexts.get(player);
		if (ctx == null) { return; }

		if (ctx.sync) { haveSyncEnabled--; }
		contexts.remove(player);
	}

	public static boolean hasDefaultModifiers(@Nullable ServerPlayer source)
	{
		return source == null || CommandsContext.get(source).modifiers.areDefault();
	}

	public static MocapModifiers getFinalModifiers(@Nullable ServerPlayer source, MocapModifiers simpleModifiers)
	{
		MocapModifiers contextModifiers = source != null ? CommandsContext.get(source).modifiers : PlaybackModifiers.DEFAULT;
		MocapModifiers modifiers = simpleModifiers.mergeWithParent(contextModifiers);

		// mergeWithParent() doesn't merge transformations, as it expects PositionTransformer to call parent transformer,
		// but before starting playback PositionTransformer doesn't exist, so we need to manually merge transformations.
		// In this case it's quite easy, as simpleModifiers should always use default (empty) transformations,
		// so we can just copy them from contextModifiers (these set with "/mocap playback modifiers").
		// Simple modifiers are these defined as optional arguments of "/mocap playback start" command.
		return modifiers.withTransformations(contextModifiers.getTransformations());
	}

	public boolean setSync(boolean sync)
	{
		boolean oldSync = this.sync;
		this.sync = sync;

		if (oldSync != sync)
		{
			if (sync) { haveSyncEnabled++; }
			else { haveSyncEnabled--; }
		}
		return oldSync;
	}

	public boolean getSync()
	{
		return sync;
	}
}
