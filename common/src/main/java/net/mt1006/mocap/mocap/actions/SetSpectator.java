package net.mt1006.mocap.mocap.actions;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapStateAction;
import net.mt1006.mocap.mocap.settings.Settings;

public class SetSpectator implements MocapStateAction
{
	private final boolean isSpectator;

	public SetSpectator(Entity entity)
	{
		isSpectator = entity instanceof ServerPlayer && (((ServerPlayer)entity).gameMode.getGameModeForPlayer() == GameType.SPECTATOR);
	}

	public SetSpectator(Reader reader)
	{
		isSpectator = reader.readBoolean();
	}

	@Override public boolean differs(MocapStateAction previousAction)
	{
		return isSpectator != ((SetSpectator)previousAction).isSpectator;
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addBoolean(isSpectator);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (!(ctx.getEntity() instanceof ServerPlayer)) { return Result.IGNORED; }
		((ServerPlayer)ctx.getEntity()).setGameMode(isSpectator
				? GameType.SPECTATOR
				: (Settings.USE_CREATIVE_GAME_MODE.val ? GameType.CREATIVE : GameType.SURVIVAL));
		return Result.OK;
	}
}
