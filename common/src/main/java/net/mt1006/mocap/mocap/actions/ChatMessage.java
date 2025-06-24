package net.mt1006.mocap.mocap.actions;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.mocap.settings.Settings;

import java.util.List;
import java.util.UUID;

public class ChatMessage implements MocapAction
{
	private final String messageJson;

	public ChatMessage(String messageJson)
	{
		this.messageJson = messageJson;
	}

	public ChatMessage(Reader reader)
	{
		this.messageJson = reader.readString();
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addString(messageJson);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (!Settings.CHAT_PLAYBACK.val) { return Result.IGNORED; }
		ServerPlayer player = ctx.getPlayerOrDummy();
		if (player == null) { return Result.IGNORED; }

		MinecraftServer server = ctx.getLevel().getServer();
		Component message = Component.Serializer.fromJson(messageJson, server.registryAccess());
		if (message == null) { return Result.IGNORED; }

		UUID senderUUID;
		if (player != ctx.getEntity())
		{
			// get UUID of any player, because dummy players are only on a server
			List<ServerPlayer> playerList = ctx.getLevel().getServer().getPlayerList().getPlayers();
			if (playerList.isEmpty()) { return Result.IGNORED; }
			senderUUID = playerList.get(0).getUUID();
		}
		else
		{
			senderUUID = player.getUUID();
		}

		PlayerChatMessage chatMessage = PlayerChatMessage.unsigned(senderUUID, message.getString()).withUnsignedContent(message);
		server.getPlayerList().broadcastChatMessage(chatMessage, player, ChatType.bind(ChatType.CHAT, player));
		return Result.OK;
	}
}
