package net.mt1006.mocap.api.v1.extension;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import org.jetbrains.annotations.Nullable;

public interface MocapRecordingData
{
	Vec3 getStartPos();

	Item itemFromId(int id);

	int provideItemId(Item item);

	BlockState blockStateFromId(int id);

	int provideBlockStateId(BlockState blockState);

	@Nullable MocapExtension getExtension(byte idFromRecording);

	byte getIdForExtension(MocapExtension extension);

	interface ExtensionHeader
	{
		void save(MocapAction.Writer writer);

		boolean load(MocapAction.Reader reader);
	}
}
