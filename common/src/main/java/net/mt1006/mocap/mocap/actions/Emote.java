package net.mt1006.mocap.mocap.actions;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.api.events.client.ClientEmoteAPI;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.utils.FakePlayer;

import java.util.UUID;

public class Emote implements MocapAction {

    private Animation animation;

    public Emote() {
        this.animation = null;
    }

    public Emote(Animation animation) {
        this.animation = animation;
    }

    public Emote(Reader reader) {
        try {
            UUID emoteId = UUID.fromString(reader.readString());
            for(Animation animation : ClientEmoteAPI.clientEmoteList()) {
                if(animation.get().equals(emoteId)) {
                    this.animation = animation;
                }
             }
        } catch (Exception e) {
            animation = null;
        }
    }

    @Override
    public void write(Writer writer, MocapRecordingData data) {
        if(animation != null) {
            writer.addString(animation.get().toString());
        }
    }

    @Override
    public Result execute(MocapActionContext ctx) {
        if(ctx.getEntity() instanceof FakePlayer fakePlayer) {
            // null stop the current animation
            ServerEmoteAPI.forcePlayEmote(fakePlayer.getUUID(), animation);
            return Result.OK;
        }
        return Result.IGNORED;
    }
}
