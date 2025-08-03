package net.mt1006.mocap.mocap.playing.modifiers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.api.v1.modifiers.MocapPlayerAsEntity;
import net.mt1006.mocap.api.v1.modifiers.MocapPlayerSkin;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.files.SceneFiles;
import net.mt1006.mocap.utils.Utils;
import org.jetbrains.annotations.Nullable;

public class PlaybackModifiers
{
	public @Nullable String playerName;
	public MocapPlayerSkin playerSkin;
	public Transformations transformations;
	public MocapPlayerAsEntity playerAsEntity;
	public StartDelay startDelay;
	public EntityFilter entityFilter;

	private PlaybackModifiers(@Nullable String playerName, MocapPlayerSkin playerSkin, Transformations transformations,
							  MocapPlayerAsEntity playerAsEntity, StartDelay startDelay, EntityFilter entityFilter)
	{
		this.playerName = playerName;
		this.playerSkin = playerSkin;
		this.transformations = transformations;
		this.playerAsEntity = playerAsEntity;
		this.startDelay = startDelay;
		this.entityFilter = entityFilter;
	}

	public PlaybackModifiers(SceneFiles.Reader reader)
	{
		playerName = reader.readString("player_name");
		playerSkin = new PlayerSkin(reader.readObject("player_skin"));
		transformations = Transformations.fromObject(reader.readObject("transformations"));
		playerAsEntity = new PlayerAsEntity(reader.readObject("player_as_entity"));
		startDelay = StartDelay.fromSeconds(reader.readDouble("start_delay", 0.0));
		entityFilter = EntityFilter.fromString(reader.readString("entity_filter"));
	}

	public static PlaybackModifiers empty()
	{
		return new PlaybackModifiers(null, PlayerSkin.DEFAULT, Transformations.empty(),
				PlayerAsEntity.DISABLED, StartDelay.ZERO, EntityFilter.FOR_PLAYBACK);
	}

	public PlaybackModifiers mergeWithParent(PlaybackModifiers parent)
	{
		return new PlaybackModifiers(
				playerName != null ? playerName : parent.playerName,
				playerSkin.mergeWithParent(parent.playerSkin),
				transformations.mergeWithParent(parent.transformations),
				playerAsEntity.isEnabled() ? playerAsEntity : parent.playerAsEntity,
				//startDelay.add(parent.startDelay), //TODO: fix how delaying start works?
				startDelay,
				!entityFilter.isDefaultForPlayback() ? entityFilter : parent.entityFilter);
	}

	public PlaybackModifiers copy()
	{
		return new PlaybackModifiers(playerName, playerSkin, transformations.copy(), playerAsEntity, startDelay, entityFilter);
	}

	public boolean areDefault()
	{
		return playerName == null && playerSkin.getSource() == MocapPlayerSkin.Source.DEFAULT
				&& transformations.areDefault() && !playerAsEntity.isEnabled() && startDelay == StartDelay.ZERO
				&& entityFilter.isDefaultForPlayback();
	}

	public void save(SceneFiles.Writer writer)
	{
		writer.addString("player_name", playerName);
		writer.addObject("player_skin", playerSkin.save());
		writer.addObject("transformations", transformations.save());
		writer.addObject("player_as_entity", playerAsEntity.save());
		writer.addDouble("start_delay", startDelay.seconds, 0.0);
		writer.addString("entity_filter", entityFilter.save());
	}

	public void list(CommandOutput out)
	{
		if (playerName == null) { out.sendSuccess("scenes.element_info.player_name.default"); }
		else { out.sendSuccess("scenes.element_info.player_name.custom", playerName); }

		switch (playerSkin.getSource())
		{
			case DEFAULT:
				out.sendSuccess("scenes.element_info.skin.default");
				break;

			case FROM_PLAYER:
				out.sendSuccess("scenes.element_info.skin.profile", playerSkin.getPath());
				break;

			case FROM_FILE:
				out.sendSuccess("scenes.element_info.skin.file", playerSkin.getPath());
				break;

			case FROM_MINESKIN:
				out.sendSuccess("scenes.element_info.skin.mineskin");
				Component urlComponent = Utils.getOpenUrlComponent(playerSkin.getPath(),
						Component.literal(String.format("  (§n%s§r)", playerSkin.getPath())));
				out.sendSuccessComponent(urlComponent);
				break;
		}

		transformations.list(out);
		out.sendSuccess("scenes.element_info.start_delay", startDelay.seconds, startDelay.ticks);

		if (!playerAsEntity.isEnabled()) { out.sendSuccess("scenes.element_info.player_as_entity.disabled"); }
		else { out.sendSuccess("scenes.element_info.player_as_entity.enabled", playerAsEntity.getRawEntityId()); }

		if (entityFilter.isDefaultForPlayback()) { out.sendSuccess("scenes.element_info.entity_filter.disabled"); }
		else { out.sendSuccess("scenes.element_info.entity_filter.enabled", entityFilter.save()); }
	}

	public boolean modify(FullCommandInfo info, String propertyName, int propertyNodePosition) throws CommandSyntaxException
	{
		switch (propertyName)
		{
			case "start_delay":
				startDelay = StartDelay.fromSeconds(info.getDouble("delay"));
				return true;

			case "transformations":
				String transformationType = info.getNode(propertyNodePosition + 1);
				if (transformationType == null) { break; }
				return transformations.modify(info, transformationType, propertyNodePosition + 1);

			case "player_name":
				playerName = info.getString("player_name");
				return true;

			case "player_skin":
				PlayerSkin newPlayerSkin = info.getPlayerSkin();
				if (newPlayerSkin == null) { return false; }

				playerSkin = newPlayerSkin;
				return true;

			case "player_as_entity":
				String playerAsEntityMode = info.getNode(propertyNodePosition + 1);
				if (playerAsEntityMode == null) { break; }

				if (playerAsEntityMode.equals("enabled"))
				{
					String playerAsEntityId = ResourceArgument.getEntityType(info.ctx, "entity").key().location().toString();

					Tag tag;
					try { tag = NbtTagArgument.getNbtTag(info.ctx, "nbt"); }
					catch (Exception e) { tag = null; }
					CompoundTag nbt = (tag instanceof CompoundTag) ? (CompoundTag)tag : null;

					playerAsEntity = new PlayerAsEntity(playerAsEntityId, nbt != null ? nbt.toString() : null);
					return true;
				}
				else if (playerAsEntityMode.equals("disabled"))
				{
					playerAsEntity = PlayerAsEntity.DISABLED;
					return true;
				}
				break;

			case "entity_filter":
				String filterMode = info.getNode(propertyNodePosition + 1);
				if (filterMode == null) { break; }

				if (filterMode.equals("enabled"))
				{
					String filterStr = info.getString("entity_filter");
					EntityFilterInstance filterInstance = EntityFilterInstance.create(filterStr);
					if (filterInstance == null)
					{
						info.sendFailure("failure.entity_filter.failed_to_parse");
						return false;
					}

					entityFilter = new EntityFilter(filterInstance);
					return true;
				}
				else if (filterMode.equals("disabled"))
				{
					entityFilter = EntityFilter.FOR_PLAYBACK;
					return true;
				}
				break;
		}
		return false;
	}

	public static boolean checkIfProperName(CommandOutput out, @Nullable String name)
	{
		if (name == null) { return true; }

		if (name.length() > 16)
		{
			out.sendFailure("failure.improper_player_name");
			out.sendFailure("failure.improper_player_name.too_long");
			return false;
		}

		if (name.contains(" "))
		{
			out.sendFailure("failure.improper_player_name");
			out.sendFailure("failure.improper_player_name.contains_spaces");
			return false;
		}
		return true;
	}
}
