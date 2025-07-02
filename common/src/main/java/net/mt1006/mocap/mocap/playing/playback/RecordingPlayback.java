package net.mt1006.mocap.mocap.playing.playback;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.impl.modifiers.MocapModifiersImpl;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.command.io.CommandInfo;
import net.mt1006.mocap.events.PlayerConnectionEvent;
import net.mt1006.mocap.mocap.files.RecordingData;
import net.mt1006.mocap.mocap.files.SceneData;
import net.mt1006.mocap.mocap.playing.DataManager;
import net.mt1006.mocap.mocap.playing.modifiers.PlaybackModifiers;
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

	private RecordingPlayback(CommandInfo commandInfo, @Nullable RecordingData recording, MocapPlaybackConfig config, PlaybackModifiers parentModifiers,
							  @Nullable SceneData.Subscene subscene, @Nullable PositionTransformer parentTransformer) throws StartException
	{
		super(subscene == null, commandInfo.getLevel(), commandInfo.getSourcePlayer(), config, parentModifiers, subscene);

		if (recording == null) { throw new StartException(); } //TODO: test if gives error message (especially as subscene)
		this.recording = recording;

		GameProfile profile = getGameProfile(commandInfo);
		if (profile == null)
		{
			commandInfo.sendFailure("playback.start.error");
			commandInfo.sendFailure("playback.start.error.profile");
			throw new StartException();
		}

		GameProfile newProfile = new GameProfile(UUID.randomUUID(), profile.getName());
		try
		{
			PropertyMap oldPropertyMap = (PropertyMap)Fields.gameProfileProperties.get(profile);
			PropertyMap newPropertyMap = (PropertyMap)Fields.gameProfileProperties.get(newProfile);

			newPropertyMap.putAll(oldPropertyMap);
			modifiers.playerSkin.addSkinToPropertyMap(commandInfo, newPropertyMap);
		}
		catch (Exception ignore) {}

		PlayerList packetTargets = level.getServer().getPlayerList();
		Entity entity;
		FakePlayer ghost = null;

		Vec3 center = modifiers.transformations.calculateCenter(recording.startPos);
		PositionTransformer transformer = new PositionTransformer(modifiers.transformations, parentTransformer, center);

		if (!modifiers.playerAsEntity.isEnabled())
		{
			FakePlayer fakePlayer = new FakePlayer(level, newProfile, this);
			entity = fakePlayer;

			EntityData.PLAYER_SKIN_PARTS.set(fakePlayer, (byte)0b01111111);
			fakePlayer.gameMode.changeGameModeForPlayer(Settings.USE_CREATIVE_GAME_MODE.val ? GameType.CREATIVE : GameType.SURVIVAL);
			recording.initEntityPosition(fakePlayer, transformer);
			recording.preExecute(new PreExecuteContext(recording, entity, level, config, MocapModifiersImpl.ofCopy(modifiers), transformer));
			modifiers.transformations.scale.applyToPlayer(fakePlayer);

			packetTargets.broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
			level.addNewPlayer(fakePlayer);

			if (!config.getCanPushEntities())
			{
				for (ServerPlayer player : PlayerConnectionEvent.players)
				{
					MocapPacketS2C.sendNocolPlayerAdd(player, fakePlayer.getUUID());
					PlayerConnectionEvent.addNocolPlayer(fakePlayer.getUUID());
				}
			}

			EntityData.PLAYER_SKIN_PARTS.set(fakePlayer, (byte)0b01111111);
		}
		else
		{
			entity = modifiers.playerAsEntity.createEntity(level);

			if (entity == null)
			{
				commandInfo.sendFailure("playback.start.warning.unknown_entity", modifiers.playerAsEntity.entityId);
				throw new StartException();
			}

			recording.initEntityPosition(entity, transformer);
			entity.setDeltaMovement(0.0, 0.0, 0.0);
			entity.setInvulnerable(config.getInvulnerablePlayback());
			entity.setNoGravity(true);
			if (entity instanceof Mob) { ((Mob)entity).setNoAi(true); }
			modifiers.transformations.scale.applyToPlayer(entity);

			level.addFreshEntity(entity);
			recording.preExecute(new PreExecuteContext(recording, entity, level, config, MocapModifiersImpl.ofCopy(modifiers), transformer));

			if (Settings.ALLOW_GHOSTS.val)
			{
				ghost = new FakePlayer(level, newProfile, this);
				ghost.gameMode.changeGameModeForPlayer(Settings.USE_CREATIVE_GAME_MODE.val ? GameType.CREATIVE : GameType.SURVIVAL);
				recording.initEntityPosition(ghost, transformer);
				level.addNewPlayer(ghost);
			}
		}

		this.ctx = new ActionContext(recording, owner, packetTargets, entity, config, modifiers, ghost, transformer);
	}

	protected static @Nullable RecordingPlayback startRoot(CommandInfo commandInfo, @Nullable RecordingData recording,
														   MocapPlaybackConfig config, PlaybackModifiers modifiers)
	{
		try { return new RecordingPlayback(commandInfo, recording, config, modifiers, null, null); }
		catch (StartException e) { return null; }
	}

	protected static @Nullable RecordingPlayback startSubscene(CommandInfo commandInfo, DataManager dataManager, MocapPlaybackConfig config,
															   Playback parent, SceneData.Subscene info)
	{
		try { return new RecordingPlayback(commandInfo, dataManager.getRecording(info.name), config, parent.modifiers, info, parent.getPosTransformer()); }
		catch (StartException e) { return null; }
	}

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

		if (root && finished && dyingTicks == 0) { stop(); }

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

	private @Nullable GameProfile getGameProfile(CommandInfo commandInfo)
	{
		String profileName = modifiers.playerName;
		Entity entity = commandInfo.getSourceEntity();
		Level level = commandInfo.getLevel();

		if (profileName == null)
		{
			if (config.getStartAsRecorded() && recording.playerName != null) { profileName = recording.playerName; }
			else if (entity instanceof ServerPlayer) { profileName = ((ServerPlayer)entity).getGameProfile().getName(); }
			else if (!level.players().isEmpty()) { profileName = level.players().get(0).getGameProfile().getName(); }
			else { profileName = "Player"; }
		}

		return ProfileUtils.getGameProfile(commandInfo.getServer(), profileName);
	}
}
