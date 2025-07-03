package net.mt1006.mocap.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.mocap.actions.ChatMessage;
import net.mt1006.mocap.mocap.recording.Recording;
import net.mt1006.mocap.mocap.recording.RecordingContext;

public class ChatEvent
{
	public static void onChatMessage(Component message, ServerPlayer sender)
	{
		if (Recording.isActive())
		{
			for (RecordingContext ctx : Recording.byRecordedPlayer(sender))
			{
				if (ctx.config.getChatRecording())
				{
					ctx.addAction(new ChatMessage(message));
				}
			}
		}
	}
}
