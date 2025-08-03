package net.mt1006.mocap.api.impl.extenstion;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.mt1006.mocap.api.v1.extension.MocapActiveRecordingActions;
import net.mt1006.mocap.api.v1.extension.MocapExtension;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.mocap.actions.ActionType;
import net.mt1006.mocap.mocap.recording.RecordingManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Supplier;

public class MocapExtensionImpl implements MocapExtension
{
	private final String id;
	private final short version;
	private Supplier<MocapRecordingData.ExtensionHeader> headerSupplier = DefaultHeader.INSTANCE_SUPPLIER;
	private final ActionType.Registry actionRegistry = new ActionType.Registry(this);

	public MocapExtensionImpl(String id, short version)
	{
		this.id = id;
		this.version = version;
	}

	@Override public String getId()
	{
		return id;
	}

	@Override public short getVersion()
	{
		return version;
	}

	@Override public void registerAction(int id, MocapAction.FromReader fromReader)
	{
		actionRegistry.register(id, fromReader, null);
	}

	@Override public void registerStateAction(int id, MocapAction.FromReader fromReader, MocapAction.FromEntity fromEntity)
	{
		actionRegistry.register(id, fromReader, fromEntity);
	}

	@Override public void setHeaderSupplier(@Nullable Supplier<MocapRecordingData.ExtensionHeader> headerSupplier)
	{
		this.headerSupplier = headerSupplier != null ? headerSupplier : DefaultHeader.INSTANCE_SUPPLIER;
	}

	@Override public Collection<? extends MocapActiveRecordingActions> findRecordedByPlayer(Player player)
	{
		return RecordingManager.byRecordedPlayer(player);
	}

	@Override public Collection<? extends MocapActiveRecordingActions.TrackedEntity> findTrackedEntities(Entity entity)
	{
		return RecordingManager.listTrackedEntities(entity);
	}

	@Override public MocapRecordingData.ExtensionHeader createHeader()
	{
		return headerSupplier.get();
	}

	@Override public ActionType.Registry getActionRegistry()
	{
		return actionRegistry;
	}

	private static class DefaultHeader implements MocapRecordingData.ExtensionHeader
	{
		private static final DefaultHeader INSTANCE = new DefaultHeader();
		private static final Supplier<MocapRecordingData.ExtensionHeader> INSTANCE_SUPPLIER = () -> INSTANCE;

		private DefaultHeader() {}

		@Override public void save(MocapAction.Writer writer)
		{
			writer.addByte((byte)0);
		}

		@Override public boolean load(MocapAction.Reader reader)
		{
			reader.readByte();
			return true;
		}
	}
}
