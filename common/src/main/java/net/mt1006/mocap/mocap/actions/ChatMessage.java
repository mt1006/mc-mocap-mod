package net.mt1006.mocap.mocap.actions;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;

import java.util.List;
import java.util.UUID;

public class ChatMessage implements MocapAction
{
	private final String messageJson;

	public ChatMessage(Component component)
	{
		String message;
		try { message = ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, component).getOrThrow().toString(); }
		catch (IllegalStateException e) { message = "{}"; }
		this.messageJson = message;
	}

	public ChatMessage(Reader reader)
	{
		reader.readByte(); // ignored
		this.messageJson = reader.readString();
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addByte((byte)0); // dummy value - for future uses
		writer.addString(messageJson);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (!ctx.getConfig().getChatPlayback()) { return Result.IGNORED; }
		ServerPlayer player = ctx.getRealOrDummyPlayer();
		if (player == null) { return Result.IGNORED; }

		MinecraftServer server = ctx.getLevel().getServer();
		Component message;
		try { message = ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, new JsonParser().parse(messageJson)).getOrThrow().getFirst(); }
		catch (Exception e) { return Result.IGNORED; }
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
