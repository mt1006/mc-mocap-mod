package net.mt1006.mocap.mocap.actions;

import com.mojang.datafixers.util.Pair;
import net.mt1006.mocap.api.v1.extension.MocapExtension;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapStateAction;
import net.mt1006.mocap.command.converter.AlphaConverter;
import net.mt1006.mocap.command.converter.AlphaMovement;
import net.mt1006.mocap.mocap.actions.deprecated.HeadRotation;
import net.mt1006.mocap.mocap.actions.deprecated.MovementLegacy;
import net.mt1006.mocap.mocap.actions.deprecated.SetEffectColor;
import net.mt1006.mocap.mocap.files.RecordingData;
import net.mt1006.mocap.mocap.files.RecordingFiles;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ActionType
{
	NEXT_TICK(0, (reader) -> NextTick.INSTANCE),
	MOVEMENT_LEGACY(1, MovementLegacy::new), // deprecated
	HEAD_ROTATION(2, HeadRotation::new), // deprecated
	CHANGE_POSE(3, ChangePose::new, ChangePose::new),
	CHANGE_ITEM(4, ChangeItem::new, ChangeItem::fromEntity),
	SET_ENTITY_FLAGS(5, SetEntityFlags::new, SetEntityFlags::new),
	SET_LIVING_ENTITY_FLAGS(6, SetLivingEntityFlags::new, SetLivingEntityFlags::fromEntity),
	SET_MAIN_HAND(7, SetMainHand::new, SetMainHand::fromEntity),
	SWING(8, Swing::new, Swing::fromEntity),
	BREAK_BLOCK(9, BreakBlock::new),
	PLACE_BLOCK(10, PlaceBlock::new),
	RIGHT_CLICK_BLOCK(11, (MocapAction.FromReaderOnly)RightClickBlock::new),
	SET_EFFECT_COLOR(12, SetEffectColor::new), // deprecated
	SET_ARROW_COUNT(13, SetArrowCount::new, SetArrowCount::fromEntity),
	SLEEP(14, Sleep::new, Sleep::new),
	PLACE_BLOCK_SILENTLY(15, PlaceBlockSilently::new),
	ENTITY_UPDATE(16, EntityUpdate::new),
	ENTITY_ACTION(17, EntityAction::new),
	HURT(18, Hurt::skipByteAndGetInstance),
	VEHICLE_DATA(19, VehicleData::new, VehicleData::new),
	BREAK_BLOCK_PROGRESS(20, (MocapAction.FromReaderOnly)BreakBlockProgress::new),
	MOVEMENT(21, Movement::new),
	SKIP_TICKS(22, SkipTicks::new),
	DIE(23, (reader) -> Die.INSTANCE),
	RESPAWN(24, (reader) -> Respawn.INSTANCE),
	CHAT_MESSAGE(25, ChatMessage::new),
	SET_SPECTATOR(26, SetSpectator::new, SetSpectator::new),
	DUMMY(27, (reader) -> DummyAction.INSTANCE),
	CLOSE_CONTAINER(28, (reader) -> CloseContainer.INSTANCE),
	SET_EFFECT_PARTICLES(29, SetEffectParticles::new, SetEffectParticles::fromEntity),
	SET_IS_BABY(30, SetIsBaby::read, SetIsBaby::fromEntity);

	public final byte id;

	ActionType(int id, MocapAction.FromReaderOnly fromReaderOnly)
	{
		this(id, (reader, data) -> fromReaderOnly.apply(reader));
	}

	ActionType(int id, MocapAction.FromReaderOnly fromReaderOnly, @Nullable MocapAction.FromEntity fromEntity)
	{
		this(id, (reader, data) -> fromReaderOnly.apply(reader), fromEntity);
	}

	ActionType(int id, MocapAction.FromReader fromReader)
	{
		this.id = (byte)id;
		Registry.MAIN.register(id, fromReader, null);
	}

	ActionType(int id, MocapAction.FromReader fromReader, @Nullable MocapAction.FromEntity fromEntity)
	{
		this.id = (byte)id;
		Registry.MAIN.register(id, fromReader, fromEntity);
	}

	private void init() { /* dummy */ }

	public static void initTypes()
	{
		if (Registry.MAIN.actions.isEmpty())
		{
			for (ActionType type : ActionType.values()) { type.init(); }
		}
	}

	public static void prepareToWriteAction(RecordingData data, MocapAction action)
	{
		if (Registry.mainIdMap.get(action.getClass()) == Registry.CUSTOM_ACTION_ID)
		{
			Pair<MocapExtension, Byte> extensionActionData = Registry.extensionActionMap.get(action.getClass());
			MocapExtension extension = extensionActionData.getFirst();

			if (data.getExtensionId(extension) == null) { data.initAndAddExtension(extension); }
		}

		action.prepareWrite(data);
	}

	public static void writeAction(MocapAction.Writer writer, MocapRecordingData data, MocapAction action)
	{
		byte id = Registry.mainIdMap.get(action.getClass());
		writer.addByte(id);

		if (id == Registry.CUSTOM_ACTION_ID)
		{
			Pair<MocapExtension, Byte> extensionActionData = Registry.extensionActionMap.get(action.getClass());
			Byte extensionId = data.getExtensionId(extensionActionData.getFirst());
			if (extensionId == null) { throw new RuntimeException("Extension wasn't initialized!"); }

			writer.addByte(extensionId);
			writer.addByte(extensionActionData.getSecond());

			RecordingFiles.Writer actionWriter = new RecordingFiles.Writer();
			action.write(actionWriter, data);
			writer.addPackedSize(actionWriter.getSize());
			actionWriter.copyToWriter(writer);
		}
		else
		{
			action.write(writer, data);
		}
	}

	//TODO: [CONVERTER] remove two last args
	public static @Nullable MocapAction readAction(MocapAction.Reader reader, MocapRecordingData data,
												   @Nullable AlphaConverter converter, @Nullable Integer converterEntityId)
	{
		Registry registry = Registry.MAIN;

		byte id = reader.readByte();

		//TODO: [CONVERTER] remove
		// =========
		if (converter != null)
		{
			if (id == MOVEMENT.id)
			{
				AlphaMovement alphaMovement = new AlphaMovement(reader);
				double[] posArray = converter.getPosArray(converterEntityId);
				return posArray != null ? alphaMovement.convert(data.getStartPos(), posArray) : null;
			}
			else if (id == CHAT_MESSAGE.id)
			{
				return new ChatMessage(reader.readString());
			}
			else if (id == ENTITY_ACTION.id)
			{
				return new EntityAction(reader, data, converter);
			}
			else if (id == ENTITY_UPDATE.id)
			{
				return new EntityUpdate(reader, converter, 0);
			}
		}
		// =========

		if (id == Registry.CUSTOM_ACTION_ID)
		{
			MocapExtension extension = data.getExtension(reader.readByte());
			id = reader.readByte();

			int size = reader.readPackedSize();
			if (extension == null)
			{
				reader.shift(size);
				return DummyAction.INSTANCE;
			}

			registry = extension.getActionRegistry();
		}

		MocapAction.FromReader constructor = registry.actions.get(Byte.toUnsignedInt(id));
		return constructor != null ? constructor.apply(reader, data) : null;
	}

	public static class Registry
	{
		private static final Registry MAIN = new Registry(null);
		private static final byte CUSTOM_ACTION_ID = (byte)255;

		public static final List<MocapAction.FromEntity> allComparableActions = new ArrayList<>();
		public static final Map<Class<?>, Byte> mainIdMap = new HashMap<>();
		private static final Map<Class<?>, Pair<MocapExtension, Byte>> extensionActionMap = new HashMap<>();

		private final @Nullable MocapExtension extension;
		private final List<MocapAction.FromReader> actions = new ArrayList<>();
		private int lastRegisteredId = -1;

		public Registry(@Nullable MocapExtension extension)
		{
			this.extension = extension;
		}

		public void register(int id, MocapAction.FromReader fromReader, @Nullable MocapAction.FromEntity fromEntity)
		{
			MocapAction instance = fromReader.apply(RecordingFiles.DUMMY_READER, RecordingData.DUMMY);
			if (fromEntity == null && instance instanceof MocapStateAction)
			{
				throw new RuntimeException("Tried to register MocapStateAction without \"fromEntity\" constructor!");
			}

			if (id > 254) { throw new RuntimeException("Tried to register an Action with ID higher than 254!"); }
			if (id != lastRegisteredId + 1) { throw new RuntimeException("Tried to register an Action with id out of order!"); }
			lastRegisteredId = id;

			if (fromEntity != null) { allComparableActions.add(fromEntity); }
			mainIdMap.put(instance.getClass(), (byte)(extension == null ? id : 255));
			extensionActionMap.put(instance.getClass(), Pair.of(extension, (byte)id));
			actions.add(fromReader);
		}
	}
}
