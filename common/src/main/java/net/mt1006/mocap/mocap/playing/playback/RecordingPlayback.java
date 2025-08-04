package net.mt1006.mocap.mocap.playing.playback;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.api.v1.modifiers.MocapPlayerSkin;
import net.mt1006.mocap.events.PlayerConnectionEvent;
import net.mt1006.mocap.mocap.files.RecordingData;
import net.mt1006.mocap.mocap.settings.Settings;
import net.mt1006.mocap.network.MocapPacketS2C;
import net.mt1006.mocap.utils.*;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class RecordingPlayback extends Playback
{
	private final RecordingData recording;
	private final ActionContext ctx;
	private int pos = 0;
	private int dyingTicks = 0;

	private RecordingPlayback(boolean isRoot, ServerLevel level, @Nullable ServerPlayer owner, MocapPlaybackConfig config,
							  MocapModifiers modifiers, RecordingData recording, ActionContext ctx)
	{
		super(isRoot, level, owner, config, modifiers);
		this.recording = recording;
		this.ctx = ctx;
	}

	public static @Nullable RecordingPlayback start(CommandInfo info, boolean isRoot, RecordingData recording, MocapPlaybackConfig config,
													MocapModifiers modifiers, @Nullable PositionTransformer parentTransformer)
	{
		if (recording == null) { throw new RuntimeException("Provided recording data is null!"); }

		GameProfile oldProfile = getGameProfile(info, modifiers.getPlayerName(), recording.playerName, config.getStartAsRecorded());
		if (oldProfile == null)
		{
			info.sendFailure("playback.start.error");
			info.sendFailure("playback.start.error.profile");
			return null;
		}
		GameProfile newProfile = createNewProfile(info, oldProfile, modifiers.getPlayerSkin());

		ServerLevel level = info.getLevel();
		PlayerList packetTargets = info.getServer().getPlayerList();
		Entity entity;
		FakePlayer ghost = null;

		Vec3 center = modifiers.getTransformations().calculateCenter(recording.startPos);
		PositionTransformer transformer = new PositionTransformer(modifiers.getTransformations(), parentTransformer, center);

		if (!modifiers.getPlayerAsEntity().isEnabled())
		{
			FakePlayer fakePlayer = new FakePlayer(level, newProfile, config.getInvulnerablePlayback());
			entity = fakePlayer;

			fakePlayer.gameMode.changeGameModeForPlayer(Settings.USE_CREATIVE_GAME_MODE.val ? GameType.CREATIVE : GameType.SURVIVAL);
			recording.initEntityPosition(fakePlayer, transformer);
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

			recording.initEntityPosition(entity, transformer);
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
				recording.initEntityPosition(ghost, transformer);
				level.addNewPlayer(ghost);
			}
		}

		ActionContext ctx = new ActionContext(recording, info.getSourcePlayer(), packetTargets, entity, config, modifiers, ghost, transformer);
		RecordingPlayback playback = new RecordingPlayback(isRoot, info.getLevel(), info.getSourcePlayer(), config, modifiers, recording, ctx);

		if (entity instanceof FakePlayer) { ((FakePlayer)entity).playback = playback; }
		else if (ghost != null) { ghost.playback = playback; }

		return playback;
	}

	private static @Nullable GameProfile getGameProfile(CommandInfo info, @Nullable String profileName,
														@Nullable String recordedName, boolean startAsRecorded)
	{
		Entity entity = info.getSourceEntity();
		Level level = info.getLevel();

		if (profileName == null)
		{
			if (startAsRecorded && recordedName != null) { profileName = recordedName; }
			else if (entity instanceof ServerPlayer) { profileName = ((ServerPlayer)entity).getGameProfile().getName(); }
			else if (!level.players().isEmpty()) { profileName = level.players().get(0).getGameProfile().getName(); }
			else { profileName = "Player"; }
		}

		return ProfileUtils.getGameProfile(info.getServer(), profileName);
	}

	private static GameProfile createNewProfile(CommandInfo info, GameProfile oldProfile, MocapPlayerSkin playerSkin)
	{
		// duplicates oldProfile but with random UUID and proper player skin
		GameProfile newProfile = new GameProfile(UUID.randomUUID(), oldProfile.getName());
		try
		{
			PropertyMap oldPropertyMap = (PropertyMap)Fields.gameProfileProperties.get(oldProfile);
			PropertyMap newPropertyMap = (PropertyMap)Fields.gameProfileProperties.get(newProfile);

			newPropertyMap.putAll(oldPropertyMap);
			playerSkin.addSkinToPropertyMap(info, newPropertyMap);
		}
		catch (Exception ignore) {}

		return newProfile;
	}

	/*protected static @Nullable RecordingPlayback startRoot(CommandInfo info, @Nullable RecordingData recording,
														   MocapPlaybackConfig config, PlaybackModifiers modifiers)
	{
		try { return new RecordingPlayback(info, recording, config, modifiers, null, null); }
		catch (StartException e) { return null; }
	}

	protected static @Nullable RecordingPlayback startSubscene(CommandInfo info, DataManager dataManager, MocapPlaybackConfig config,
															   Playback parent, SceneData.Subscene subscene)
	{
		try { return new RecordingPlayback(info, dataManager.getRecording(subscene.name), config, parent.modifiers, subscene, parent.getPosTransformer()); }
		catch (StartException e) { return null; }
	}*/

	@Override public boolean tick()
	{
		if (dyingTicks > 0)
		{
			dyingTicks--;
			if (dyingTicks == 0)
			{
				ctx.removeMainEntity();
				stop();
				return true;
			}
			return false;
		}
		if (finished) { return true; }

		if (shouldExecuteTick())
		{
			while (true)
			{
				MocapAction.Result result = recording.executeNext(ctx, config, pos++);

				if (result.endsPlayback)
				{
					if (result == MocapAction.Result.ERROR)
					{
						Utils.sendMessage(owner, "error.playback_error");
						MocapMod.LOGGER.error("Something went wrong during playback!");
					}
					else if (recording.endsWithDeath)
					{
						if (ctx.entity instanceof FakePlayer) { ((FakePlayer)ctx.entity).fakeKill(); }
						else { ctx.entity.kill(null); }

						if (ctx.entity instanceof LivingEntity) { dyingTicks = 20; }
					}
					finished = true;
				}

				if (result == MocapAction.Result.REPEAT_TICK) { pos--; }
				if (result.endsTick) { break; }
			}
		}

		if (isRoot && finished && dyingTicks == 0) { stop(); }

		tickCounter++;
		return finished && dyingTicks == 0;
	}

	@Override public void stop()
	{
		ctx.removeEntities();
		finished = true;
	}

	@Override public boolean wasFinished()
	{
		return finished && dyingTicks == 0;
	}

	@Override protected PositionTransformer getPosTransformer()
	{
		return ctx.transformer;
	}
}
