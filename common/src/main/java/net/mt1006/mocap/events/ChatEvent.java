package net.mt1006.mocap.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.mocap.actions.ChatMessage;
import net.mt1006.mocap.mocap.recording.RecordingContext;
import net.mt1006.mocap.mocap.recording.RecordingManager;

public class ChatEvent
{
	public static void onChatMessage(Component message, ServerPlayer sender)
	{
		if (RecordingManager.isActive())
		{
			for (RecordingContext ctx : RecordingManager.byRecordedPlayer(sender))
			{
				if (ctx.config.getChatRecording())
				{
					ctx.addAction(new ChatMessage(Component.Serializer.toJson(message, sender.server.registryAccess())));
				}
			}
		}
	}
}
