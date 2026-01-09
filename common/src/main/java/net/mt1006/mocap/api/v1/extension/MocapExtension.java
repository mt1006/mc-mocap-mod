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
	String getId();

	short getVersion();

	boolean isRequired();

	void registerAction(int id, MocapAction.FromReader fromReader);

	void registerStateAction(int id, MocapAction.FromReader fromReader, MocapAction.FromEntity fromEntity);

	void setHeaderSupplier(@Nullable Supplier<MocapRecordingData.ExtensionHeader> headerSupplier);

	boolean isRecordingActive();

	Collection<? extends MocapRecordingContext> findRecordingByRecordedPlayer(Player player);

	Collection<? extends MocapRecordingContext> findRecordingByRecordedPlayerUUID(UUID uuid);

	Collection<? extends MocapRecordingContext.TrackedEntity> findTrackedEntities(Entity entity);

	@ApiStatus.Internal
	MocapRecordingData.ExtensionHeader createHeader();

	@ApiStatus.Internal
	ActionType.Registry getActionRegistry();
}
