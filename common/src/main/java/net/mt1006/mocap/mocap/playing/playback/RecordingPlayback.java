package net.mt1006.mocap.mocap.playing.playback;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.v1.controller.config.MocapDimensionSource;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.controller.playable.MocapRecordingFile;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.api.v1.modifiers.MocapPlayerSkin;
import net.mt1006.mocap.events.PlayerConnectionEvent;
import net.mt1006.mocap.mocap.actions.Die;
import net.mt1006.mocap.mocap.files.RecordingData;
import net.mt1006.mocap.mocap.settings.Settings;
import net.mt1006.mocap.network.MocapPacketS2C;
import net.mt1006.mocap.utils.EntityData;
import net.mt1006.mocap.utils.FakePlayer;
import net.mt1006.mocap.utils.ProfileUtils;
import net.mt1006.mocap.utils.Utils;
import org.jetbrains.annotations.Nullable;

public class RecordingPlayback extends Playback
{
	private final RecordingData recording;
	private final ActionContext ctx;
	private int pos = 0;

	private RecordingPlayback(boolean isRoot, @Nullable ServerPlayer owner, MocapPlaybackConfig config,
							  MocapModifiers modifiers, RecordingData recording, ActionContext ctx)
	{
		super(isRoot, owner, config, modifiers);
		this.recording = recording;
		this.ctx = ctx;
	}

	public static @Nullable RecordingPlayback start(CommandInfo info, boolean isRoot, RecordingData recording, MocapPlaybackConfig config,
													MocapModifiers modifiers, @Nullable PositionTransformer parentTransformer)
	{
		if (recording == null) { throw new RuntimeException("Provided recording data is null!"); }

		ProfileUtils.Profile oldProfile = getGameProfile(info, modifiers.getPlayerName(), recording.assignedProfile, config.getStartAsRecorded());
		GameProfile newProfile = createNewProfile(info, oldProfile.name, oldProfile.skin, modifiers.getPlayerSkin());

		ServerLevel level = getLevel(info, recording, config.getDimensionSource());
		PlayerList packetTargets = info.getServer().getPlayerList();
		Entity entity;
		FakePlayer ghost = null;

		Vec3 center = modifiers.getTransformations().calculateCenter(recording.startPos);
		PositionTransformer transformer = new PositionTransformer(modifiers.getTransformations(), parentTransformer, center);
		boolean delayedStart = (modifiers.getTimeModifiers().getStartDelay().ticks != 0);

		if (!modifiers.getPlayerAsEntity().isEnabled())
		{
			FakePlayer fakePlayer = new FakePlayer(level, newProfile, config.getInvulnerablePlayback());
			entity = fakePlayer;

			fakePlayer.gameMode.changeGameModeForPlayer(Settings.USE_CREATIVE_GAME_MODE.val ? GameType.CREATIVE : GameType.SURVIVAL);
			recording.initEntityPosition(fakePlayer, transformer, delayedStart);
			modifiers.getTransformations().applyScaleToPlayer(fakePlayer);

			packetTargets.broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
			level.addNewPlayer(fakePlayer);

			recording.preExecute(new PreExecuteContext(recording, entity, level, config, modifiers, transformer));
			EntityData.PLAYER_SKIN_PARTS.set(fakePlayer, (byte)0b01111111);

			if (!config.getCanPushEntities())
			{
				for (ServerPlayer player : PlayerConnectionEvent.players)
				{
					MocapPacketS2C.sendNocolPlayerAdd(player, fakePlayer.getUUID());
					PlayerConnectionEvent.addNocolPlayer(fakePlayer.getUUID());
				}
			}
		}
		else
		{
			entity = modifiers.getPlayerAsEntity().createEntity(level);
			if (entity == null)
			{
				info.sendFailure("playback.start.warning.unknown_entity", modifiers.getPlayerAsEntity().getRawEntityId());
				return null;
			}

			recording.initEntityPosition(entity, transformer, delayedStart);
			entity.setDeltaMovement(0.0, 0.0, 0.0);
			entity.setInvulnerable(config.getInvulnerablePlayback());
			entity.setNoGravity(true);
			if (entity instanceof Mob) { ((Mob)entity).setNoAi(true); }
			modifiers.getTransformations().applyScaleToPlayer(entity);

			level.addFreshEntity(entity);
			recording.preExecute(new PreExecuteContext(recording, entity, level, config, modifiers, transformer));

			if (Settings.ALLOW_GHOSTS.val)
			{
				ghost = new FakePlayer(level, newProfile, config.getInvulnerablePlayback());
				ghost.gameMode.changeGameModeForPlayer(Settings.USE_CREATIVE_GAME_MODE.val ? GameType.CREATIVE : GameType.SURVIVAL);
				recording.initEntityPosition(ghost, transformer, delayedStart);
				level.addNewPlayer(ghost);
			}
		}

		ActionContext ctx = new ActionContext(recording, info.getSourcePlayer(), packetTargets, entity, config, modifiers, ghost, transformer);
		RecordingPlayback playback = new RecordingPlayback(isRoot, info.getSourcePlayer(), config, modifiers, recording, ctx);

		if (entity instanceof FakePlayer) { ((FakePlayer)entity).playback = playback; }
		else if (ghost != null) { ghost.playback = playback; }

		return playback;
	}

	private static ServerLevel getLevel(CommandInfo info, RecordingData recording, MocapDimensionSource dimensionSource)
	{
		if (recording.dimensionId == null)
		{
			return switch (dimensionSource)
			{
				case ASSIGNED_OR_CURRENT, CURRENT -> info.getLevel();
				case ASSIGNED_OR_OVERWORLD, OVERWORLD -> info.getServer().overworld();
			};
		}
		else
		{
			return switch (dimensionSource)
			{
				case ASSIGNED_OR_CURRENT, ASSIGNED_OR_OVERWORLD -> info.getServer()
						.getLevel(ResourceKey.create(Registries.DIMENSION, recording.dimensionId));
				case CURRENT -> info.getLevel();
				case OVERWORLD -> info.getServer().overworld();
			};
		}
	}

	private static ProfileUtils.Profile getGameProfile(CommandInfo info, @Nullable String profileName,
														MocapRecordingFile.AssignedProfile recordedProfile, boolean startAsRecorded)
	{
		Entity entity = info.getSourceEntity();
		PlayerList playerList = info.getServer().getPlayerList();

		if (profileName == null)
		{
			if (startAsRecorded && recordedProfile.name() != null && recordedProfile.skinValue() != null && recordedProfile.skinSignature() != null)
			{
				return ProfileUtils.Profile.withSkin(recordedProfile.name(),
						new Property("textures", recordedProfile.skinValue(), recordedProfile.skinSignature()));
			}

			if (startAsRecorded && recordedProfile.name() != null) { profileName = recordedProfile.name(); }
			else if (entity instanceof ServerPlayer) { profileName = ((ServerPlayer)entity).getGameProfile().name(); }
			else if (!playerList.getPlayers().isEmpty()) { profileName = playerList.getPlayers().get(0).getGameProfile().name(); }
			else { profileName = "Player"; }
		}

		return ProfileUtils.getProfile(info.getServer(), profileName, true);
	}

	private static GameProfile createNewProfile(CommandInfo info, String name, @Nullable Property oldSkinProperty, MocapPlayerSkin playerSkin)
	{
		Property skinProperty = playerSkin.getSkinProperty(info, oldSkinProperty);
		Property customSkinProperty = playerSkin.getCustomSkinProperty();
		return ProfileUtils.createGameProfile(name, skinProperty, customSkinProperty);
	}

	@Override public void tick()
	{
		if (finished) { return; }

		if (shouldExecuteTick())
		{
			if (waitOnEnd != 0)
			{
				if (waitOnEnd == 1) { finished = true; }
				waitOnEnd--;
			}
			else
			{
				int startDelay = modifiers.getTimeModifiers().getStartDelay().ticks;
				int waitOnStart = modifiers.getTimeModifiers().getWaitOnStart().ticks;

				if (startDelay == tickCounter)
				{
					boolean delayedStart = (modifiers.getTimeModifiers().getStartDelay().ticks != 0);
					if (delayedStart) { recording.initEntityPosition(ctx.getEntity(), ctx.getTransformer(), false); }

					recording.firstExecute(ctx.getEntity());
					tickInitialActions();
				}
				if (startDelay + waitOnStart <= tickCounter) { tickActions(); }
			}
			tickCounter++;
		}

		if (finished && modifiers.getTimeModifiers().getLoop()) { loop(); }
		else if (shouldSelfStop()) { stop(); }
	}

	private void tickInitialActions()
	{
		int tempPos = 0;
		while (true)
		{
			MocapAction.Result result = recording.executeAction(ctx, config, true, tempPos++);
			switch (result)
			{
				case OK, IGNORED:
					break;

				case NEXT_TICK, REPEAT_TICK, END:
					return;

				case ERROR:
					Utils.sendMessage(owner, "error.playback_error");
					MocapMod.LOGGER.error("Something went wrong during initial tick!");
					finished = true;
					return;

				default:
					throw new IllegalStateException("Unexpected value: " + result);
			}
		}
	}

	private void tickActions()
	{
		while (true)
		{
			MocapAction.Result result = recording.executeAction(ctx, config, false, pos++);
			switch (result)
			{
				case OK, IGNORED:
					break;

				case NEXT_TICK:
					return;

				case REPEAT_TICK:
					pos--;
					return;

				case END:
					if (recording.endsWithDeath) { Die.INSTANCE.execute(ctx); }
					finishOrWaitOnEnd();
					return;

				case ERROR:
					Utils.sendMessage(owner, "error.playback_error");
					MocapMod.LOGGER.error("Something went wrong during playback!");
					finished = true;
					return;

				default:
					throw new IllegalStateException("Unexpected value: " + result);
			}
		}
	}

	@Override public void stop()
	{
		if (!stopped)
		{
			ctx.removeMainEntity();
			ctx.removeAdditionalEntities();
			finished = true;
			stopped = true;
		}
	}

	@Override protected void loop()
	{
		boolean delayedStart = (modifiers.getTimeModifiers().getStartDelay().ticks != 0);
		recording.initEntityPosition(ctx.getEntity(), ctx.getTransformer(), delayedStart);

		ctx.removeAdditionalEntities();
		pos = 0;
		tickCounter = 0;
		finished = false;
	}
}
