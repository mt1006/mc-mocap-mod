package net.mt1006.mocap.mocap.playing.modifiers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.api.v1.modifiers.*;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.files.SceneFiles;
import net.mt1006.mocap.utils.Utils;
import org.jetbrains.annotations.Nullable;

public class PlaybackModifiers implements MocapModifiers
{
	public static final PlaybackModifiers DEFAULT = new PlaybackModifiers(null, PlayerSkin.DEFAULT,
			Transformations.EMPTY, PlayerAsEntity.DISABLED, TimeModifiers.DEFAULT, EntityFilter.FOR_PLAYBACK);
	public final @Nullable String playerName;
	public final MocapPlayerSkin playerSkin;
	public final MocapTransformations transformations;
	public final MocapPlayerAsEntity playerAsEntity;
	public final MocapTimeModifiers timeModifiers;
	public final MocapEntityFilter entityFilter;

	private PlaybackModifiers(@Nullable String playerName, MocapPlayerSkin playerSkin, MocapTransformations transformations,
							  MocapPlayerAsEntity playerAsEntity, MocapTimeModifiers timeModifiers, MocapEntityFilter entityFilter)
	{
		this.playerName = playerName;
		this.playerSkin = playerSkin;
		this.transformations = transformations;
		this.playerAsEntity = playerAsEntity;
		this.timeModifiers = timeModifiers;
		this.entityFilter = entityFilter;
	}

	public PlaybackModifiers(SceneFiles.Reader reader)
	{
		this.playerName = reader.readString("player_name");
		this.playerSkin = PlayerSkin.fromObject(reader.readObject("player_skin"));
		this.transformations = Transformations.fromObject(reader.readObject("transformations"));
		this.playerAsEntity = PlayerAsEntity.fromObject(reader.readObject("player_as_entity"));
		this.timeModifiers = TimeModifiers.fromObject(reader.readObject("time"));
		this.entityFilter = EntityFilter.fromString(reader.readString("entity_filter"));
	}

	@Override public @Nullable String getPlayerName()
	{
		return playerName;
	}

	@Override public MocapModifiers withPlayerName(@Nullable String name)
	{
		return new PlaybackModifiers(name, playerSkin, transformations, playerAsEntity, timeModifiers, entityFilter);
	}

	@Override public MocapPlayerSkin getPlayerSkin()
	{
		return playerSkin;
	}

	@Override public MocapModifiers withPlayerSkin(MocapPlayerSkin skin)
	{
		return new PlaybackModifiers(playerName, skin, transformations, playerAsEntity, timeModifiers, entityFilter);
	}

	@Override public MocapTransformations getTransformations()
	{
		return transformations;
	}

	@Override public MocapModifiers withTransformations(MocapTransformations transformations)
	{
		return new PlaybackModifiers(playerName, playerSkin, transformations, playerAsEntity, timeModifiers, entityFilter);
	}

	@Override public MocapPlayerAsEntity getPlayerAsEntity()
	{
		return playerAsEntity;
	}

	@Override public MocapModifiers withPlayerAsEntity(MocapPlayerAsEntity playerAsEntity)
	{
		return new PlaybackModifiers(playerName, playerSkin, transformations, playerAsEntity, timeModifiers, entityFilter);
	}

	@Override public MocapTimeModifiers getTimeModifiers()
	{
		return timeModifiers;
	}

	@Override public MocapModifiers withTimeModifiers(MocapTimeModifiers timeModifiers)
	{
		return new PlaybackModifiers(playerName, playerSkin, transformations, playerAsEntity, timeModifiers, entityFilter);
	}

	@Override public MocapEntityFilter getEntityFilter()
	{
		return entityFilter;
	}

	@Override public MocapModifiers withEntityFilter(MocapEntityFilter filter)
	{
		return new PlaybackModifiers(playerName, playerSkin, transformations, playerAsEntity, timeModifiers, filter);
	}

	@Override public boolean areDefault()
	{
		return playerName == null && playerSkin.getSource() == MocapPlayerSkin.Source.DEFAULT
				&& transformations.areDefault() && !playerAsEntity.isEnabled() && timeModifiers.areDefault()
				&& entityFilter.isDefaultForPlayback();
	}

	@Override public MocapModifiers mergeWithParent(MocapModifiers parent)
	{
		return new PlaybackModifiers(
				playerName != null ? playerName : parent.getPlayerName(),
				playerSkin.mergeWithParent(parent.getPlayerSkin()),
				transformations.mergeWithParent(parent.getTransformations()),
				playerAsEntity.isEnabled() ? playerAsEntity : parent.getPlayerAsEntity(),
				timeModifiers.mergeWithParent(parent.getTimeModifiers()),
				!entityFilter.isDefaultForPlayback() ? entityFilter : parent.getEntityFilter());
	}

	@Override public void save(SceneFiles.Writer writer)
	{
		writer.addString("player_name", playerName);
		writer.addObject("player_skin", playerSkin.save());
		writer.addObject("transformations", transformations.save());
		writer.addObject("player_as_entity", playerAsEntity.save());
		writer.addObject("time", timeModifiers.save());
		writer.addString("entity_filter", entityFilter.save());
	}

	@Override public void list(CommandOutput out)
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
		timeModifiers.list(out);

		if (!playerAsEntity.isEnabled()) { out.sendSuccess("scenes.element_info.player_as_entity.disabled"); }
		else { out.sendSuccess("scenes.element_info.player_as_entity.enabled", playerAsEntity.getRawEntityId()); }

		if (entityFilter.isDefaultForPlayback()) { out.sendSuccess("scenes.element_info.entity_filter.disabled"); }
		else { out.sendSuccess("scenes.element_info.entity_filter.enabled", entityFilter.save()); }
	}

	@Override public @Nullable MocapModifiers modify(FullCommandInfo info, String propertyName, int propertyNodePos) throws CommandSyntaxException
	{
		switch (propertyName)
		{
			case "time":
				MocapTimeModifiers newTimeModifiers = timeModifiers.modify(info, propertyNodePos + 1);
				return newTimeModifiers != null ? withTimeModifiers(newTimeModifiers) : null;

			case "transformations":
				MocapTransformations newTransformations = transformations.modify(info, propertyNodePos + 1);
				return newTransformations != null ? withTransformations(newTransformations) : null;

			case "player_name":
				String playerNameType = info.getNode(propertyNodePos + 1);
				return switch (playerNameType)
				{
					case "inherited" -> withPlayerName(null);
					case "blank" -> withPlayerName("");
					case "set" -> withPlayerName(info.getString("player_name"));
					case null, default -> null;
				};

			case "player_skin":
				MocapPlayerSkin newPlayerSkin = info.getPlayerSkin();
				return newPlayerSkin != null ? withPlayerSkin(newPlayerSkin) : null;

			case "player_as_entity":
				String playerAsEntityMode = info.getNode(propertyNodePos + 1);
				if (playerAsEntityMode == null) { return null; }

				if (playerAsEntityMode.equals("disabled"))
				{
					return withPlayerAsEntity(PlayerAsEntity.DISABLED);
				}
				else if (playerAsEntityMode.equals("enabled"))
				{
					String playerAsEntityId = ResourceArgument.getEntityType(info.ctx, "entity").key().location().toString();

					Tag tag;
					try { tag = NbtTagArgument.getNbtTag(info.ctx, "nbt"); }
					catch (Exception e) { tag = null; }
					CompoundTag nbt = (tag instanceof CompoundTag) ? (CompoundTag)tag : null;

					return withPlayerAsEntity(new PlayerAsEntity(playerAsEntityId, nbt != null ? nbt.toString() : null));
				}
				return null;

			case "entity_filter":
				String filterMode = info.getNode(propertyNodePos + 1);
				if (filterMode == null) { return null; }

				if (filterMode.equals("disabled"))
				{
					return withEntityFilter(EntityFilter.FOR_PLAYBACK);
				}
				else if (filterMode.equals("enabled"))
				{
					String filterStr = info.getString("entity_filter");
					EntityFilterInstance filterInstance = EntityFilterInstance.create(filterStr);
					if (filterInstance == null)
					{
						info.sendFailure("failure.entity_filter.failed_to_parse");
						return null;
					}

					return withEntityFilter(new EntityFilter(filterInstance));
				}
				return null;

			default:
				return null;
		}
	}

	public static boolean checkIfProperPlayerName(CommandOutput out, @Nullable String name)
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
