package net.mt1006.mocap.mocap.recording;

import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

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

	public static RecordingSource forCommand(CommandInfo info)
	{
		ServerPlayer sourcePlayer = info.getSourcePlayer();
		return sourcePlayer != null ? new RecordingSource(sourcePlayer, sourcePlayer.getName().getString()) : DEFAULT;
	}

	public static RecordingSource forAPI(String name)
	{
		return new RecordingSource(null, String.format(Locale.ROOT, "+%s", name));
	}
}
