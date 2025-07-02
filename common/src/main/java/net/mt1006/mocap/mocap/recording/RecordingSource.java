package net.mt1006.mocap.mocap.recording;

import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.command.io.CommandInfo;
import org.jetbrains.annotations.Nullable;

public class RecordingSource
{
	private static final RecordingSource DEFAULT = new RecordingSource(null, "+mc");
	public final @Nullable ServerPlayer player;
	public final String name;

	private RecordingSource(@Nullable ServerPlayer player, String name)
	{
		this.player = player;
		this.name = name;
	}

	public static RecordingSource forCommand(CommandInfo commandInfo)
	{
		ServerPlayer sourcePlayer = commandInfo.getSourcePlayer();
		return sourcePlayer != null ? new RecordingSource(sourcePlayer, sourcePlayer.getName().getString()) : DEFAULT;
	}

	public static RecordingSource forAPI(String name)
	{
		return new RecordingSource(null, String.format("+%s", name));
	}
}
