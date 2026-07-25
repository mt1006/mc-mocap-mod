package net.mt1006.mocap.api.v1.extension;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.mocap.actions.ActionType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;

public interface MocapExtension
{
	/**
	 * @return ID of an extension
	 */
	String getId();

	/**
	 * @return version of an extension
	 */
	short getVersion();

	/**
	 * @return is extension required
	 * @see net.mt1006.mocap.api.v1.MocapAPI#registerExtension(String, short, boolean)
	 */
	boolean isRequired();

	/**
	 * Register new action type. <b>Registered action cannot be state action.
	 * For state actions use registerStateAction() instead.</b>
	 * @param id unique (for a given extension) ID of an action in range [0, 254]
	 * @param fromReader action constructor like {@code MyAction(Reader reader, MocapRecordingData data)}
	 * @see MocapAction.FromReader
	 * @see MocapExtension#registerStateAction
	 */
	void registerAction(int id, MocapAction.FromReader fromReader);

	void registerStateAction(int id, MocapAction.FromReader fromReader, MocapAction.FromEntity fromEntity);

	void setHeaderSupplier(@Nullable Supplier<MocapRecordingData.ExtensionHeader> headerSupplier);

	/**
	 * Check if there are any active recordings.
	 * It could be used as the first check in event callbacks to exit
	 * without unnecessary creating action instance.
	 * @return true if there's any active recording, false otherwise
	 */
	boolean isRecordingActive();

	/**
	 * @param player player
	 * @return collection of recording contexts of given player
	 */
	Collection<? extends MocapRecordingContext> findRecordingByRecordedPlayer(Player player);

	/**
	 * @param uuid UUID of a player
	 * @return collection of recording contexts of player with given UUID
	 */
	Collection<? extends MocapRecordingContext> findRecordingByRecordedPlayerUUID(UUID uuid);

	/**
	 * @param entity given entity
	 * @return collection of all TrackedEntity instances for a given entity
	 */
	Collection<? extends MocapRecordingContext.TrackedEntity> findTrackedEntities(Entity entity);

	@ApiStatus.Internal
	MocapRecordingData.ExtensionHeader createHeader();

	@ApiStatus.Internal
	ActionType.Registry getActionRegistry();
}
