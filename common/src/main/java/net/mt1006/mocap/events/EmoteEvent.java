package net.mt1006.mocap.events;

import io.github.kosmx.emotes.api.events.server.ServerEmoteEvents;
import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.mocap.actions.Emote;
import net.mt1006.mocap.mocap.recording.Recording;

public class EmoteEvent {

    private static final ServerEmoteEvents.EmotePlayEvent emotePlayEvent = (animation, v, uuid) -> {
        if(Recording.isActive()) {
            ServerPlayer serverPlayer = MocapMod.server.getPlayerList().getPlayer(uuid);
            Recording.byRecordedPlayer(serverPlayer).forEach((ctx) -> ctx.addAction(new Emote(animation)));
        }
    };

    private static final ServerEmoteEvents.EmoteStopEvent emoteStopEvent = (uuid, uuid1) -> {
        if(Recording.isActive()) {
            ServerPlayer serverPlayer = MocapMod.server.getPlayerList().getPlayer(uuid1);
            Recording.byRecordedPlayer(serverPlayer).forEach((ctx) -> ctx.addAction(new Emote()));
        }
    };

    public static void listen() {
        ServerEmoteEvents.EMOTE_PLAY.register(emotePlayEvent);
        ServerEmoteEvents.EMOTE_STOP_BY_USER.register(emoteStopEvent);
    }

    public static void unListen() {
        ServerEmoteEvents.EMOTE_PLAY.unregister(emotePlayEvent);
        ServerEmoteEvents.EMOTE_STOP_BY_USER.unregister(emoteStopEvent);
    }
}
