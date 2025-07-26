package net.mt1006.mocap.mocap.actions;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.utils.FakePlayer;

import java.util.UUID;

public class Emote implements MocapAction {

    private Animation animation;

    public Emote() {}

    public Emote(Animation animation) {
        this.animation = animation;
    }

    public Emote(Reader reader) {
        try {
            UUID uuid = UUID.fromString(reader.readString());
            animation = UniversalEmoteSerializer.getEmote(uuid);
        } catch (Exception ignored) {}
    }

    @Override
    public void write(Writer writer, MocapRecordingData data) {
        if(animation == null) {
            writer.addString("");
        } else {
            writer.addString(animation.data().get("uuid").get().toString());
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
